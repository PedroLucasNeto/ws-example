package com.messenger.chat.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.messenger.chat.model.Message;
import com.messenger.chat.service.MessageService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageRestController {
    private final MessageService service;

    public MessageRestController(MessageService service) {
        this.service = service;
    }

    // GET /api/messages?with=Alice&date=2025-11-27
    @GetMapping
    public List<Message> getMessages(@RequestParam(required = false) String with,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {

        // If explicit date range provided, use range query and then filter by
        // participant
        if (dateFrom != null && dateTo != null) {
            List<Message> inRange = service.findByDate(dateFrom.toLocalDateTime(), dateTo.toLocalDateTime());
            return inRange.stream()
                    .filter(m -> with == null || with.equals(m.getSender()) || with.equals(m.getRecipient()))
                    .collect(Collectors.toList());
        }

        // If single date provided, treat as day filter
        if (date != null) {
            OffsetDateTime from = date.atStartOfDay().atOffset(ZoneOffset.UTC);
            return service.getConversation(with, null, from);
        }

        // Default: if 'with' is provided, return conversation; otherwise return all
        // messages
        if (with == null) {
            return service.getAllMessages();
        }
        return service.getConversation(with, null, null);
    }
}
