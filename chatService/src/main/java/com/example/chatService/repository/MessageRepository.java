package com.example.chatService.repository;

import com.example.chatService.dto.response.MessageProjection;
import com.example.chatService.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<MessageProjection> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);
}