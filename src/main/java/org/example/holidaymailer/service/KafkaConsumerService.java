package org.example.holidaymailer.service;

import org.example.holidaymailer.entity.EmailMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaConsumerService {

    @Autowired
    private EmailService emailService;


    @KafkaListener(topics = "email-task", groupId = "email-group")
    public void consume(EmailMessage message) {
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendEmail(message);
            } catch (Exception e) {
            }
        });
    }
}

