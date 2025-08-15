package com.example.apiGateway.config;

import com.example.apiGateway.repository.AuthClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebClientConfiguration {
    @Bean
    WebClient authWebClient(){
        return WebClient.builder()
                .baseUrl("http://localhost:8081/auth")
                .build();
    }

    @Bean
    AuthClient authClient(WebClient authWebClient){
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(authWebClient))
                .build();

        return httpServiceProxyFactory.createClient(AuthClient.class);
    }
}
