package com.example.userService.repository;

import com.example.userService.dto.response.NotMemberResponse;
import com.example.userService.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    Boolean existsByUserId(String userId);

    Optional<UserProfile> findByUserId(String userId);

    void deleteByUserId(String userId);

     @Query("SELECT u FROM UserProfile u WHERE u.userId LIKE %:keyword% OR u.fullName LIKE %:keyword% OR u.email LIKE %:keyword%")
     List<UserProfile> searchProfilesByKeyword(@Param("keyword") String keyword);

//    @Query("SELECT u FROM UserProfile u WHERE u.userId NOT IN (:memberIds)")
//    List<UserProfile> getUserNotMemberInProject(@Param("memberIds") List<String> memberIds);

    @Query("SELECT u FROM UserProfile u WHERE u.userId NOT IN (:memberIds)")
    List<NotMemberResponse> getUserNotMemberInProject(@Param("memberIds") List<String> memberIds);
}
