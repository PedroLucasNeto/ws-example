package com.messenger.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.messenger.chat.model.Message;
import com.messenger.chat.service.MessageService;
import com.messenger.chat.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
public class ChatWebSocketController {

    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(MessageService messageService, UserService userService,
            SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    // Recebe mensagens do destino /app/chat
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Message receiveMessage(Message incoming) {
        // marcar usuário como online (caso envie sem registrar)
        if (incoming.getSender() != null) {
            userService.registerOrUpdateOnline(incoming.getSender());
        }
        Message saved = messageService.saveMessage(incoming);
        return saved; // broadcast para /topic/messages
    }

    // Endpoint alternativo para buscar mensagens por data via websocket (opcional)
    @MessageMapping("/messagesByDate")
    public void messagesByDate(String date) {
        LocalDate d = LocalDate.parse(date);
        LocalDateTime start = d.atStartOfDay();
        LocalDateTime end = d.atTime(LocalTime.MAX);
        List<Message> msgs = messageService.findByDate(start, end);
        messagingTemplate.convertAndSend("/topic/messages-by-date", msgs);
    }
}
