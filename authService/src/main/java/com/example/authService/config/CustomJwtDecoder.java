package com.example.authService.config;

import com.example.authService.dto.request.IntrospectRequest;
import com.example.authService.dto.response.IntrospectResponse;
import com.example.authService.service.AuthenticationService;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public Jwt decode(String token) throws JwtException {
        IntrospectResponse response = authenticationService.introspect(
                new IntrospectRequest(token)
        );

        if (!response.isValid()) {
            throw new JwtException("Invalid token");
        }

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return new Jwt(
                    token,
                    signedJWT.getJWTClaimsSet().getIssueTime().toInstant(),
                    signedJWT.getJWTClaimsSet().getExpirationTime().toInstant(),
                    signedJWT.getHeader().toJSONObject(),
                    signedJWT.getJWTClaimsSet().getClaims());
        } catch (ParseException e) {
            throw new JwtException("Invalid token");
        }

    }
}
