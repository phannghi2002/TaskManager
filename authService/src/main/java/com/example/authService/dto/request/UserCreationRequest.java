package com.example.authService.dto.request;

import com.example.authService.dto.response.RoleResponse;
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
    String email;
    String password;

    Set<String> roles;

    String firstName;
    String lastName;
    LocalDate dob;
    String city;
}
