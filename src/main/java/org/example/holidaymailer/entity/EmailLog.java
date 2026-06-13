package org.example.holidaymailer.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "email_log")
public class EmailLog {
    @Id
    private String taskId;

    private String recipient;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String status;

    private int retryCount;

    private LocalDateTime createdAt;
}

