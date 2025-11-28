package com.messenger.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.messenger.chat.model.Message;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Busca mensagens entre two users (sender/recipient) ou broadcast
    @Query("select m from Message m where " +
            "(:a is null or :b is null or ( (m.sender = :a and m.recipient = :b) or (m.sender = :b and m.recipient = :a) )) "
            +
            "and (:from is null or m.timestamp >= :from) " +
            "order by m.timestamp asc")
    List<Message> findConversation(String a, String b, OffsetDateTime from);

    List<Message> findByTimestampBetweenOrderByTimestampAsc(LocalDateTime start, LocalDateTime end);

    List<Message> findAllByOrderByTimestampAsc();
}