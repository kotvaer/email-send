package org.example.holidaymailer.service;

import org.example.holidaymailer.entity.EmailMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendEmail(EmailMessage message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(message.to());
        mail.setSubject(message.subject());
        mail.setText(message.content());
        mailSender.send(mail);
    }


}
