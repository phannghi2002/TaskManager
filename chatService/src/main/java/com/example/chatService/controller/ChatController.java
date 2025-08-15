package com.example.chatService.controller;

import com.example.chatService.dto.request.ChatRoomCreationRequest;
import com.example.chatService.dto.request.ChatRoomUpdateNameRequest;
import com.example.chatService.dto.response.ApiResponse;
import com.example.chatService.entity.ChatRoom;
import com.example.chatService.service.ChatRoomService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {
    ChatRoomService chatRoomService;

    @PostMapping("/create")
    ApiResponse<ChatRoom> createChatRoom(@RequestBody ChatRoomCreationRequest request) {
        return ApiResponse.<ChatRoom>builder()
                .result(chatRoomService.createChatRoom(request))
                .build();
    }

    @PostMapping("/{chatRoomId}/toggle-member-room/{targetUserId}")
    ApiResponse<ChatRoom> addMemberInChatRoom(
            @PathVariable String chatRoomId,
            @PathVariable String targetUserId) {
        return ApiResponse.<ChatRoom>builder()
                .result(chatRoomService.toggleMemberInChatRoom(chatRoomId, targetUserId))
                .build();
    }

    @GetMapping("/{chatRoomId}")
    ApiResponse<ChatRoom> getChatRoom(@PathVariable String chatRoomId) {
        return ApiResponse.<ChatRoom>builder()
                .result(chatRoomService.getChatRoom(chatRoomId))
                .build();
    }

    @GetMapping("/all-chat-room/{userId}")
    ApiResponse<List<ChatRoom>> getAllChatRoomMySelf(
            @PathVariable String userId) {
        return ApiResponse.<List<ChatRoom>>builder()
                .result(chatRoomService.getAllChatRoom(userId))
                .build();
    }

    @DeleteMapping("/{chatRoomId}")
    ApiResponse<Void> deleteChatRoom(
            @PathVariable String chatRoomId) {
        chatRoomService.deleteChatRoom(chatRoomId);
        return ApiResponse.<Void>builder()
                .message("Delete chat room successfully.")
                .build();
    }

    @PutMapping("/update-chat-name/{chatRoomId}")
    ApiResponse<ChatRoom> updateChatRoom(
            @PathVariable String chatRoomId,
            @RequestBody ChatRoomUpdateNameRequest request) {
        return ApiResponse.<ChatRoom>builder()
                .result(chatRoomService.updateChatRoomName(chatRoomId, request.getName()))
                .build();
    }

}
