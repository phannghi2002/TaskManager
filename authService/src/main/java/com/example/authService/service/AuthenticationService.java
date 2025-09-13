package com.example.authService.service;

import com.example.authService.dto.request.*;
import com.example.authService.dto.response.ApiResponse;
import com.example.authService.dto.response.AuthenticationResponse;
import com.example.authService.dto.response.IntrospectResponse;
import com.example.authService.entity.InvalidatedToken;
import com.example.authService.entity.RefreshToken;
import com.example.authService.entity.User;
import com.example.authService.exception.AppException;
import com.example.authService.exception.ErrorCode;
import com.example.authService.repository.InvalidatedTokenRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Duration;
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
    EmailService emailService;

    UserRepository userRepository;

    InvalidatedTokenRepository invalidatedTokenRepository;

    RedisTemplate<String, RefreshToken> redisTemplate;

    RedisTemplate<String, String> customStringRedisTemplate;

    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey}")
    String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    long REFRESHABLE_DURATION;

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


        String roleName = user.getRoles().stream()
                .findFirst()
                .map(role -> role.getName())
                .orElse(null);

        return AuthenticationResponse.builder()
                .token(token)
                .expiryTime(expiryTime)
                .refreshToken(refreshToken.getToken())
                .role(roleName)
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
                .jwtID(UUID.randomUUID().toString())
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

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    @Transactional
    public void logout(LogoutRequest request) throws ParseException, JOSEException {

        log.info("djad");
        SignedJWT signedJWT = verifyToken(request.getToken());


        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();


        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jwtId).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);


        String refreshTokenKey = "refreshToken:" + request.getRefreshToken();


        if (Boolean.FALSE.equals(redisTemplate.hasKey(refreshTokenKey))) {

            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        redisTemplate.delete(refreshTokenKey);
    }

    public void forgotPassword(String userEmail) {
        String token = generateRandomToken();


        customStringRedisTemplate.opsForValue().set("password_reset:" + userEmail, token, Duration.ofMinutes(10));


        SendEmailRequest request = new SendEmailRequest();
        Recipient recipient = new Recipient();
        recipient.setEmail(userEmail);

        request.setTo(recipient);
        request.setSubject("Yêu cầu khôi phục mật khẩu của bạn");
        request.setHtmlContent("Mã khôi phục mật khẩu của bạn là: <b>" + token + "</b>. Mã này sẽ hết hạn sau 10 phút.");

        // Gọi hàm sendEmail để gửi đi
        emailService.sendEmail(request);
    }

    private String generateRandomToken() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public boolean verifyOtp(String email, String otp) {
        String redisKey = "password_reset:" + email;

        String storedOtp = customStringRedisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null || !storedOtp.equals(otp)) {
            return false;
        }

        customStringRedisTemplate.delete(redisKey);

        return true;
    }

    public boolean changePassword(String userEmail, String newPassword) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }

        return false;
    }

    public ApiResponse<String> resetPassword(ResetPasswordRequest request){
        if (!verifyOtp(request.getEmail(), request.getOtp())) {
           throw new AppException(ErrorCode.OTP_INVALID);
        }

        if (!changePassword(request.getEmail(), request.getNewPassword())) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        return ApiResponse.<String>builder()
                .message("Password change successfully")
                .build();
    }
}
