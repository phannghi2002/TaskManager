package com.example.chatService.dto.response;

import java.time.Instant;

public interface MessageProjection {
    String getId();
    String getSenderId();
    String getFullName();
    String getContent();
    Instant getCreateAt();
    Instant getUpdateAt();
    Boolean getEdit();
}
