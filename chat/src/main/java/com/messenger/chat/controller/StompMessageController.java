package com.messenger.chat.controller;

import com.messenger.chat.dto.ChatMessage;
import com.messenger.chat.model.Message;
import com.messenger.chat.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.time.OffsetDateTime;

@Controller
public class StompMessageController {
    private final MessageService messageService;

    public StompMessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        // Map DTO to entity
        Message m = Message.builder()
                .sender(message.getFrom())
                .content(message.getText())
                .timestamp(java.time.LocalDateTime.now())
                .build();

        // Persist
        Message saved = messageService.saveMessage(m);

        // Set server-side timestamp back into DTO as ISO offset string (UTC)
        OffsetDateTime ts = saved.getTimestamp().atOffset(ZoneOffset.UTC);
        message.setTime(ts.format(ISO_FMT));
        return message;
    }
}
