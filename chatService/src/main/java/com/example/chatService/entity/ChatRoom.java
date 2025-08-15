package com.example.chatService.entity;

import com.example.chatService.enums.Type;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "chat_rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRoom {
    @Id
     String id;
     String name;
     String projectId;
     List<String> userIds;
     String createBy;
     Type type;
     String lastMessage;
     Instant lastMessageTimestamp;
}