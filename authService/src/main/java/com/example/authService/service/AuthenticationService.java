package com.example.authService.service;

import com.example.authService.dto.request.AuthenticationRequest;
import com.example.authService.dto.request.IntrospectRequest;
import com.example.authService.dto.request.RefreshTokenRequest;
import com.example.authService.dto.response.AuthenticationResponse;
import com.example.authService.dto.response.IntrospectResponse;
import com.example.authService.entity.RefreshToken;
import com.example.authService.entity.User;
import com.example.authService.exception.AppException;
import com.example.authService.exception.ErrorCode;
import com.example.authService.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;

    RedisTemplate<String, RefreshToken> redisTemplate;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public AuthenticationResponse authenticate(AuthenticationRequest request) throws ParseException {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token = generateToken(user);

        Date expiryTime = getExpiryTimeOfToken(token) ;

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(REFRESHABLE_DURATION))
                .userId(user.getId())
                .build();

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("userId", refreshToken.getUserId());
        tokenData.put("expiryDate", refreshToken.getExpiryDate().toString());

        redisTemplate.opsForHash().putAll("refreshToken:" + refreshToken.getToken(), tokenData);

        redisTemplate.expire("refreshToken:" + refreshToken.getToken(), REFRESHABLE_DURATION, TimeUnit.SECONDS);

        return AuthenticationResponse.builder()
                .token(token)
                .expiryTime(expiryTime)
                .refreshToken(refreshToken.getToken())
                .build();

    }

    private Date getExpiryTimeOfToken(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);

        return signedJWT.getJWTClaimsSet().getExpirationTime();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("phannghi")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .claim("scope", buildScope(user))
                .claim("userId", user.getId())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {

            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            }) ;
        }

        return stringJoiner.toString();
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        String token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException {
        String key = "refreshToken:" + request.getRefreshToken();
        log.info("Rrrr {}", key);

        Map<Object, Object> storedTokenData = redisTemplate.opsForHash().entries(key);

        log.info("dinh {}" ,storedTokenData);

        if (storedTokenData == null || storedTokenData.isEmpty()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        String userId = (String) storedTokenData.get("userId");
        String expiryDateStr = (String) storedTokenData.get("expiryDate");

        LocalDateTime expiryDate = LocalDateTime.parse(expiryDateStr);

        if (expiryDate.isBefore(LocalDateTime.now())) {
            redisTemplate.delete(key);
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String accessToken = generateToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(REFRESHABLE_DURATION))
                .build();

        redisTemplate.delete(key);

        String newKey = "refreshToken:" + refreshToken.getToken();
        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("userId", refreshToken.getUserId());
        tokenData.put("expiryDate", refreshToken.getExpiryDate().toString());

        redisTemplate.opsForHash().putAll(newKey, tokenData);
        redisTemplate.expire(newKey, REFRESHABLE_DURATION, TimeUnit.SECONDS);


        return AuthenticationResponse.builder()
                .token(accessToken)
                .expiryTime(getExpiryTimeOfToken(accessToken))
                .refreshToken(refreshToken.getToken())
                .build();
    }


    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

}
