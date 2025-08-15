package com.example.chatService.controller;

import com.example.chatService.dto.request.MessageCreationRequest;
import com.example.chatService.dto.request.MessageUpdateRequest;
import com.example.chatService.dto.response.ApiResponse;
import com.example.chatService.dto.response.MessageProjection;
import com.example.chatService.dto.response.MessageResponse;
import com.example.chatService.entity.Message;
import com.example.chatService.service.MessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageController {
    MessageService messageService;

    @PostMapping("/create")
    ApiResponse<Message> createMessage(@RequestBody MessageCreationRequest request) {
        return ApiResponse.<Message>builder()
                .result(messageService.createMessage(request))
                .build();
    }

    @GetMapping("/get-all-message/{chatRoomId}")
    ApiResponse<List<MessageProjection>> getAllMessageInChatRoom(@PathVariable String chatRoomId) {
        return ApiResponse.<List<MessageProjection>>builder()
                .result(messageService.getAllMessageInChatRoom(chatRoomId))
                .build();
    }

    @PutMapping("/update-message/{messageId}")
    ApiResponse<MessageResponse> updateMessage(
            @PathVariable String messageId,
            @RequestBody MessageUpdateRequest request) {
        return ApiResponse.<MessageResponse>builder()
                .result(messageService.updateMessage(messageId, request))
                .build();
    }

    @DeleteMapping("/{messageId}")
    ApiResponse<Void> deleteMessage(
            @PathVariable String messageId) {
        messageService.deleteMessage(messageId);
        return ApiResponse.<Void>builder()
                .message("Delete message successfully")
                .build();
    }


}
