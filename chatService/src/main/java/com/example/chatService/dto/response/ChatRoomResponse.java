package com.example.chatService.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRoomResponse {
    String id;
    String name;
    String projectId;
    List<String> userIds;
    String type;
    String lastMessage;
    Date lastMessageTimestamp;
}