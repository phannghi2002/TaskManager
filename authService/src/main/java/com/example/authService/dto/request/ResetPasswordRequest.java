package com.example.authService.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    @Email
    @NotBlank(message = "Email is mandatory")
    String email;

    @NotBlank(message = "OTP is mandatory")
    String otp;

    @NotBlank(message = "New password is mandatory")
     String newPassword;
}
