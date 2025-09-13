package com.example.userService.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
    String id;

    String userId;
    String email;
    String fullName;
    LocalDate dob;
    LocalDateTime joinDate;
    String city;
    String role;
}