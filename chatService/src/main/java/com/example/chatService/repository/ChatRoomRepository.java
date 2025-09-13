package com.example.chatService.repository;

import com.example.chatService.entity.ChatRoom;
import com.example.chatService.enums.Type;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    boolean existsByIdAndUserIdsContaining(String chatRoomId, String userId);

    List<ChatRoom> findByUserIdsContaining(String userId);

    boolean existsByCreateBy(String createBy);

    @Query("{ 'type' : ?0, 'userIds' : { '$all' : ?1 } }")
    Optional<ChatRoom> findByTypeAndMemberIds(Type type, List<String> userIds);

}
