package com.example.chatService.service;

import com.example.chatService.dto.request.ChatRoomCreationRequest;
import com.example.chatService.dto.response.ApiResponse;
import com.example.chatService.dto.response.ProjectMemberInfoResponse;
import com.example.chatService.entity.ChatRoom;
import com.example.chatService.enums.Type;
import com.example.chatService.exception.AppException;
import com.example.chatService.exception.ErrorCode;
import com.example.chatService.repository.ChatRoomRepository;
import com.example.chatService.repository.httpclient.ProjectClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class ChatRoomService {
    ChatRoomRepository chatRoomRepository;
    ProjectClient projectClient;

    private ChatRoom findChatRoomOrThrow(String chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_ROOM_NOT_EXISTED));
    }

    private void validateMemberInChatRoom(ChatRoom chatRoom, String userId) {
        if (!chatRoom.getUserIds().contains(userId)) {
            throw new AppException(ErrorCode.NOT_MEMBER_IN_ROOM_CHAT);
        }
    }

    private void validateRoomOwner(ChatRoom chatRoom, String userId) {
        if (!chatRoom.getCreateBy().equals(userId)) {
            throw new AppException(ErrorCode.NOT_EDIT_CHAT_ROOM);
        }
    }

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

    public ChatRoom createChatRoom(ChatRoomCreationRequest request) {
        List<String> memberIds;
        Type type;
        String name;
        String currentUserId = getUserIdFromToken();

        if (request.getProjectId() != null) {
            ApiResponse<ProjectMemberInfoResponse> membersInfo =
                    projectClient.getMemberInProject(request.getProjectId());

            memberIds = membersInfo.getResult().getMemberIds();
            if (memberIds.size() < 2) {
                throw new AppException(ErrorCode.GROUP_REQUEST);
            }

            type = Type.GROUP;
            name = request.getName() != null
                    ? request.getName()
                    : membersInfo.getResult().getProjectName();

        } else if (request.getUserIds() != null && request.getUserIds().size() == 1) {
            String otherUserId = request.getUserIds().get(0);

            if (currentUserId.equals(otherUserId)) {
                throw new AppException(ErrorCode.INVALID_CREATE_CHAT_ONESELF_REQUEST);
            }
            memberIds = List.of(currentUserId, otherUserId);

            type = Type.PRIVATE;
            name = null;

        } else if (request.getUserIds() != null && request.getUserIds().size() > 1) {
            memberIds = new ArrayList<>(request.getUserIds());
            if (!memberIds.contains(currentUserId)) memberIds.add(currentUserId);

            type = Type.GROUP;
            name = Optional.ofNullable(request.getName()).orElse(String.join(", ", memberIds));

        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .name(name)
                .projectId(request.getProjectId())
                .userIds(memberIds)
                .createBy(currentUserId)
                .type(type)
                .build();

        return chatRoomRepository.save(chatRoom);
    }

    public List<ChatRoom> getAllChatRoom(String userId) {
        return chatRoomRepository.findByUserIdsContaining(userId);
    }

    public ChatRoom getChatRoom(String chatRoomId) {
        String userId = getUserIdFromToken();

        ChatRoom chatRoom = findChatRoomOrThrow(chatRoomId);

        validateMemberInChatRoom(chatRoom, userId);

        return chatRoom;
    }

    public void deleteChatRoom(String chatRoomId) {
        String userId = getUserIdFromToken();

        ChatRoom chatRoom = findChatRoomOrThrow(chatRoomId);

        validateRoomOwner(chatRoom, userId);

        chatRoomRepository.delete(chatRoom);
    }

    public ChatRoom toggleMemberInChatRoom(String chatRoomId, String targetUserId) {
        String userId = getUserIdFromToken();

        ChatRoom chatRoom = findChatRoomOrThrow(chatRoomId);

        validateRoomOwner(chatRoom, userId);


        if (!chatRoom.getType().equals(Type.GROUP)) {
            throw new AppException(ErrorCode.NOT_EDIT_MEMBER_IN_PRIVATE);
        }

        if (chatRoom.getUserIds().contains(targetUserId)) {
            chatRoom.getUserIds().remove(targetUserId);
        } else {
            chatRoom.getUserIds().add(targetUserId);
        }

        return chatRoomRepository.save(chatRoom);
    }

    public ChatRoom updateChatRoomName(String chatRoomId, String newName) {
        String userId = getUserIdFromToken();

        ChatRoom chatRoom = findChatRoomOrThrow(chatRoomId);

        validateRoomOwner(chatRoom, userId);

        chatRoom.setName(newName);
        return chatRoomRepository.save(chatRoom);
    }

    public void updateLastMessage(String chatRoomId, String message) {
        String userId = getUserIdFromToken();

        ChatRoom chatRoom = findChatRoomOrThrow(chatRoomId);

        validateMemberInChatRoom(chatRoom, userId);

        chatRoom = chatRoom.toBuilder()
                .lastMessage(message)
                .lastMessageTimestamp(Instant.now())
                .build();
        chatRoomRepository.save(chatRoom);
    }
}
