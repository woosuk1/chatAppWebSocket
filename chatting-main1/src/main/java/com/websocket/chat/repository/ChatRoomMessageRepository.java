package com.websocket.chat.repository;

import com.websocket.chat.entity.ChatRoomMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository("mongoChatRoomMessageRepository")
public interface ChatRoomMessageRepository extends MongoRepository<ChatRoomMessage, String> {
    ChatRoomMessage findByRoomId(String roomId);
}
