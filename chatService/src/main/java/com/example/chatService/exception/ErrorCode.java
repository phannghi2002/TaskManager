package com.example.chatService.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1004, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1005, "You do not have permission", HttpStatus.FORBIDDEN),
    WRONG_PASSWORD(1006, " Password not correct", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1007, "Role not founded", HttpStatus.NOT_FOUND),
    PROJECT_NOT_FOUND(1008, "Project not founded", HttpStatus.NOT_FOUND),
    USER_NOT_IN_PROJECT(1009, "User not in project", HttpStatus.NOT_FOUND),
    PROJECT_NOW_NOT_HAVE_MEMBER(1010, "Project now not have member", HttpStatus.NOT_FOUND),
    TASK_NOT_EXISTED(1011, "Task not existed", HttpStatus.NOT_FOUND),
    INVALID_REQUEST(1012, "Invalid request create chat room", HttpStatus.BAD_REQUEST),
    INVALID_CREATE_CHAT_ONESELF_REQUEST(1013, "Cannot create a chat room with oneself", HttpStatus.BAD_REQUEST),
    GROUP_REQUEST(1014, "GROUP chat phải truyền ít nhat 2 thanh vien", HttpStatus.BAD_REQUEST),
    CHAT_ROOM_NOT_EXISTED(1015, "Chat room not existed", HttpStatus.NOT_FOUND),
    NOT_MEMBER_IN_ROOM_CHAT(1016, "You are not a member of the chat room.", HttpStatus.FORBIDDEN),
    MESSAGE_NOT_EXISTED(1017, "Message not existed", HttpStatus.NOT_FOUND),
    NOT_EDIT_MESSAGE(1018, "Cannot edit or delete someone else's message.", HttpStatus.FORBIDDEN),
    NOT_EDIT_CHAT_ROOM(1019, "Cannot edit chat room because you are not the creator ", HttpStatus.FORBIDDEN),
    NOT_EDIT_MEMBER_IN_PRIVATE(1021, "Cannot edit or delete members because this chat is private", HttpStatus.BAD_REQUEST),

    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
