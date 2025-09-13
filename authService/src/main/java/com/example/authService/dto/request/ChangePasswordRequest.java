package com.example.authService.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @NotBlank(message = "Password old is mandatory")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    String oldPassword;

    @NotBlank(message = "Password new is mandatory")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    String newPassword;

    @NotBlank(message = "Password repeat new is mandatory")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    String repeatNewPassword;
}
