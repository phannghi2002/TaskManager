package com.example.chatService.service;

import com.example.chatService.dto.request.MessageCreationRequest;
import com.example.chatService.dto.request.MessageUpdateRequest;
import com.example.chatService.dto.response.MessageProjection;
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
import java.util.HashMap;
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
                .fullName(request.getFullName())
                .content(request.getContent())
                .createAt(Instant.now())
                .build();

        chatRoomService.updateLastMessage(request.getChatRoomId(), message.getContent());

        Message savedMessage =  messageRepository.save(message);

//        messagingTemplate.convertAndSend("/topic/chat/"+ request.getChatRoomId(), request.getContent());
        messagingTemplate.convertAndSend("/topic/chat/"+ request.getChatRoomId(), savedMessage);

        return savedMessage ;
    }

    public List<MessageProjection> getAllMessageInChatRoom(String chatRoomId) {
        if (!chatRoomRepository.existsById(chatRoomId))
            throw new AppException(ErrorCode.CHAT_ROOM_NOT_EXISTED);

        String senderId = getUserIdFromToken();
        if (!checkUserInChatRoom(chatRoomId, senderId)) throw new AppException(ErrorCode.NOT_MEMBER_IN_ROOM_CHAT);

        return messageRepository.findByChatRoomIdOrderByCreateAtAsc(chatRoomId);
    }

    public Message updateMessage(String messageId, MessageUpdateRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(()-> new AppException(ErrorCode.MESSAGE_NOT_EXISTED));

        String senderId = getUserIdFromToken();

        if (!senderId.equals(message.getSenderId())) {
            throw new AppException(ErrorCode.NOT_EDIT_MESSAGE);
        }

        message.setContent(request.getContent());
        message.setEdit(true);
        message.setUpdateAt(Instant.now());


        Message savedMessage = messageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/chat/" + savedMessage.getChatRoomId(), savedMessage);

        return savedMessage;

//        return MessageResponse.builder()
//                .senderId(savedMessage.getSenderId())
//                .content(savedMessage.getContent())
//                .timestamp(savedMessage.getTimestamp())
//                .build();


    }

    public void deleteMessage(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(()-> new AppException(ErrorCode.MESSAGE_NOT_EXISTED));

        String senderId = getUserIdFromToken();

        if (!senderId.equals(message.getSenderId())) {
            throw new AppException(ErrorCode.NOT_EDIT_MESSAGE);
        }
        messageRepository.deleteById(messageId);

        Map<String, String> payload = new HashMap<>();
        payload.put("type", "DELETE");
        payload.put("messageId", message.getId());

        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getChatRoomId(),
                payload
        );
    }
}
