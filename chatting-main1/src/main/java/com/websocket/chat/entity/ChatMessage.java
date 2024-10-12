package com.websocket.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@Document(collection  = "message") // com.websocket.chat.test 데이터베이스에 message 테이블 생성하여 메시지 내용 저장
public class ChatMessage {

    @Id
    private String id;
    private String roomId;
    private String sender;
    private String message;
}
