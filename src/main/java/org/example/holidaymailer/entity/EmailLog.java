package org.example.holidaymailer.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_log")
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipient;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime sendTime;

    private boolean success;

    private String errorMessage;
}

