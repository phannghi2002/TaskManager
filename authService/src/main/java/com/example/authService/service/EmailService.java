package com.example.authService.service;

import com.example.authService.dto.request.EmailRequest;
import com.example.authService.dto.request.SendEmailRequest;
import com.example.authService.dto.request.Sender;
import com.example.authService.dto.response.EmailResponse;
import com.example.authService.exception.AppException;
import com.example.authService.exception.ErrorCode;
import com.example.authService.repository.httpclient.EmailClient;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {
    EmailClient emailClient;

    @Value("${email.brevo-apikey}")
    @NonFinal
    String apiKey;

    public EmailResponse sendEmail(SendEmailRequest request) {
        EmailRequest emailRequest = EmailRequest.builder()
                .sender(Sender.builder()
                        .name("Phan Nghi")
                        .email("nghi.phandang@starack.net")
                        .build())
                .to(List.of(request.getTo()))
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .build();
        try {
            return emailClient.sendEmail(apiKey, emailRequest);
        } catch (FeignException e){
            //throw new AppException(ErrorCode.CANNOT_SEND_EMAIL);
            log.error("Failed to send email. Status: {}, Message: {}, Body: {}",
                    e.status(), e.getMessage(), e.contentUTF8());


            throw new AppException(ErrorCode.CANNOT_SEND_EMAIL);
        }
    }

}
