package com.websocket.chat.service;

import com.websocket.chat.entity.ChatRoomMessage;
import com.websocket.chat.repository.ChatRoomMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@RequiredArgsConstructor
@Service
public class ChatRoomMessageService {

    private final ChatRoomMessageRepository chatRoomMessageRepository;
    private final MongoTemplate mongoTemplate;
    private static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(FORMAT);

    // 특정 채팅방의 메시지 조회
    public ChatRoomMessage getMessagesByRoomId(String roomId) {
        return chatRoomMessageRepository.findByRoomId(roomId);
    }

    // 특정 채팅방에 메시지 추가 (업데이트 방식으로)
    public void addMessageToRoom(String roomId, String sender, String message) {
        ChatRoomMessage.Message newMessage = new ChatRoomMessage.Message();
        newMessage.setSender(sender);
        newMessage.setMessage(message);
        /* 설명. default: 안읽음, 생성시각 추가*/
        newMessage.setReadYn(false);
        newMessage.setCreatedAt(LocalDateTime.now().format(FORMATTER));


        // MongoDB 쿼리로 부분 업데이트 수행
        Query query = new Query(Criteria.where("roomId").is(roomId));
        Update update = new Update().push("messages", newMessage);

        // document가 있으면 업데이트, 없으면 생성
        mongoTemplate.upsert(query, update, ChatRoomMessage.class);
    }

    public void markMessagesAsRead(String roomId, String user) {
        Query query = new Query(Criteria.where("roomId").is(roomId).and("messages.sender").ne(user).and("messages.readYn").is(false));
        Update update = new Update().set("messages.$[].readYn", true);
        mongoTemplate.updateMulti(query, update, ChatRoomMessage.class);
    }

}
