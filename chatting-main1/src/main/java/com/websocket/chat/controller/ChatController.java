package com.websocket.chat.controller;


import com.websocket.chat.entity.ChatMessage;
import com.websocket.chat.service.ChatRoomMessageService;
import com.websocket.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("")
public class ChatController {

    private final ChatRoomMessageService chatRoomMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessage message) {
        // 메시지를 해당 roomId에 추가
        chatRoomMessageService.addMessageToRoom(message.getRoomId(), message.getSender(), message.getMessage());

        // 구독자들에게 메시지 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}

