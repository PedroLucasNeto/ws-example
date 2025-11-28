package com.messenger.chat.controller;

import com.messenger.chat.dto.ChatMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class RestChatController {

    private final SimpMessagingTemplate template;

    public RestChatController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @PostMapping
    public ChatMessage postMessage(@RequestBody ChatMessage message) {
        this.template.convertAndSend("/topic/messages", message);
        return message;
    }
}
