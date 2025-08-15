package com.example.userService.repository;

import com.example.userService.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    Boolean existsByUserId(String userId);

    Optional<UserProfile> findByUserId(String userId);

    void deleteByUserId(String userId);
}
