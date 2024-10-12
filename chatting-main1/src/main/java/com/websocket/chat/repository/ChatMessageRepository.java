package com.websocket.chat.repository;


import com.websocket.chat.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository("mongoChatMessageRepository")
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findAll();

    List<ChatMessage> findByRoomId(String roomId);
}
