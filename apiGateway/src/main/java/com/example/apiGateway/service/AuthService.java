package com.example.apiGateway.service;

import com.example.apiGateway.dto.request.IntrospectRequest;
import com.example.apiGateway.dto.response.ApiResponse;
import com.example.apiGateway.dto.response.IntrospectResponse;
import com.example.apiGateway.repository.AuthClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    AuthClient authClient;

    public Mono<ApiResponse<IntrospectResponse>> introspect(String token) {
        return authClient.introspect(IntrospectRequest.builder()
                .token(token)
                .build());
    }
}

