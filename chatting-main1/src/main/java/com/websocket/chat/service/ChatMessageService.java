package com.websocket.chat.service;

import com.websocket.chat.entity.ChatMessage;
import com.websocket.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public List<ChatMessage> getAllMessages() {
        return chatMessageRepository.findAll(); // 모든 메시지 조회
    }

    public void saveMessage(ChatMessage message) {
        chatMessageRepository.save(message); // 메시지 저장
    }

    // 특정 채팅방의 메시지를 조회하는 메소드
    public List<ChatMessage> getMessagesByRoomId(String roomId) {
        return chatMessageRepository.findByRoomId(roomId); // roomId로 메시지 조회
    }
}
