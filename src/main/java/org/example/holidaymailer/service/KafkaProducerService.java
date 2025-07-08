package org.example.holidaymailer.service;


import org.example.holidaymailer.entity.EmailMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "email-task";

    @Autowired
    private KafkaTemplate<String, EmailMessage> kafkaTemplate;

    public void sendEmailMessage(EmailMessage message) {
        kafkaTemplate.send(TOPIC, message);
    }
}
