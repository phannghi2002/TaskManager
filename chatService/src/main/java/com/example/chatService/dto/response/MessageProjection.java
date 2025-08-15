package com.example.chatService.dto.response;

import java.time.Instant;

public interface MessageProjection {
    String getSenderId();
    String getContent();
    Instant getTimestamp();
}
