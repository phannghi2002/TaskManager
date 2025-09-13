package com.example.authService.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Email
    @NotBlank(message = "Email is mandatory")
    String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    String password;

    Set<String> roles;

    @NotBlank(message = "Full name is mandatory")
    @Size(min = 2, message = "Full name must be more than 2 characters")
    String fullName;

    @Past(message = "Date of birth must be in the past")
    @NotNull(message = "Date of birth is mandatory")
    LocalDate dob;

    @NotBlank(message = "City is mandatory")
    String city;

}
