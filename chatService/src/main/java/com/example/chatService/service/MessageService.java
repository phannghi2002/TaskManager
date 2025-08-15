package com.example.chatService.service;

import com.example.chatService.dto.request.MessageCreationRequest;
import com.example.chatService.dto.request.MessageUpdateRequest;
import com.example.chatService.dto.response.MessageProjection;
import com.example.chatService.dto.response.MessageResponse;
import com.example.chatService.entity.Message;
import com.example.chatService.exception.AppException;
import com.example.chatService.exception.ErrorCode;
import com.example.chatService.repository.ChatRoomRepository;
import com.example.chatService.repository.MessageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class MessageService {
    ChatRoomRepository chatRoomRepository;
    ChatRoomService chatRoomService;
    MessageRepository messageRepository;
    SimpMessagingTemplate messagingTemplate;

    public String getUserIdFromToken() {
        var context = SecurityContextHolder.getContext();

        var authentication = context.getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();

            String subject = jwt.getSubject();
            Map<String, Object> claims = jwt.getClaims(); // All claims

            return (String) claims.get("userId");
        } else {
            throw new IllegalStateException("No JWT token found in SecurityContext");
        }
    }

    public Boolean checkUserInChatRoom(String chatRoomId, String senderId){
        return chatRoomRepository.existsByIdAndUserIdsContaining(chatRoomId, senderId);
    }

    @Transactional
    public Message createMessage(MessageCreationRequest request) {
        if (!chatRoomRepository.existsById(request.getChatRoomId()))
            throw new AppException(ErrorCode.CHAT_ROOM_NOT_EXISTED);

        String senderId = getUserIdFromToken();
        if (!checkUserInChatRoom(request.getChatRoomId(), senderId)) throw new AppException(ErrorCode.NOT_MEMBER_IN_ROOM_CHAT);

        Message message = Message.builder()
                .chatRoomId(request.getChatRoomId())
                .senderId(senderId)
                .content(request.getContent())
                .timestamp(Instant.now())
                .build();

        chatRoomService.updateLastMessage(request.getChatRoomId(), message.getContent());

        messagingTemplate.convertAndSend("/topic/chat/"+ request.getChatRoomId(), request.getContent());

        return messageRepository.save(message);
    }

    public List<MessageProjection> getAllMessageInChatRoom(String chatRoomId) {
        if (!chatRoomRepository.existsById(chatRoomId))
            throw new AppException(ErrorCode.CHAT_ROOM_NOT_EXISTED);

        String senderId = getUserIdFromToken();
        if (!checkUserInChatRoom(chatRoomId, senderId)) throw new AppException(ErrorCode.NOT_MEMBER_IN_ROOM_CHAT);

        return messageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
    }

    public MessageResponse updateMessage(String messageId, MessageUpdateRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(()-> new AppException(ErrorCode.MESSAGE_NOT_EXISTED));

        String senderId = getUserIdFromToken();

        if (!senderId.equals(message.getSenderId())) {
            throw new AppException(ErrorCode.NOT_EDIT_MESSAGE);
        }

        message.setContent(request.getContent());
        message.setTimestamp(Instant.now());
        messageRepository.save(message);

        return MessageResponse.builder()
                .senderId(message.getSenderId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }

    public void deleteMessage(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(()-> new AppException(ErrorCode.MESSAGE_NOT_EXISTED));

        String senderId = getUserIdFromToken();

        if (!senderId.equals(message.getSenderId())) {
            throw new AppException(ErrorCode.NOT_EDIT_MESSAGE);
        }
        messageRepository.deleteById(messageId);
    }
}
