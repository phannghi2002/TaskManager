package com.example.chatService.repository;

import com.example.chatService.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    boolean existsByIdAndUserIdsContaining(String chatRoomId, String userId);

    List<ChatRoom> findByUserIdsContaining(String userId);

    boolean existsByCreateBy(String createBy);
}
