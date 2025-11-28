package com.messenger.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.messenger.chat.model.Message;
import com.messenger.chat.repository.MessageRepository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional
    public Message saveMessage(Message msg) {
        if (msg.getTimestamp() == null)
            msg.setTimestamp(LocalDateTime.now());
        return messageRepository.save(msg);
    }

    public List<Message> findByDate(LocalDateTime dateStart, LocalDateTime dateEnd) {
        return messageRepository.findByTimestampBetweenOrderByTimestampAsc(dateStart, dateEnd);
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAllByOrderByTimestampAsc();
    }

    public Message save(Message m) {
        return messageRepository.save(m);
    }

    public List<Message> getConversation(String a, String b, OffsetDateTime from) {
        return messageRepository.findConversation(a, b, from);
    }
}