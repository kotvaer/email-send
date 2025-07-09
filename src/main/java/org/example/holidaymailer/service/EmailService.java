package org.example.holidaymailer.service;

import org.example.holidaymailer.config.CozeBot;
import org.example.holidaymailer.entity.EmailMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private CozeBot cozeBot;

    @Qualifier("virtualThreadExecutor")
    @Autowired
    private Executor executor;

    public void sendEmail(EmailMessage message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(message.to());
        mail.setSubject(message.subject());
        mail.setText(message.content());
        mailSender.send(mail);
    }

    private void sendEmail(String name, String email, String subject, String content) throws Exception {
        sendEmail(
                EmailMessage.of(
                        email,
                        subject,
                        content)
        );
    }

    public void sendEmailGenFromBot(String name, String email, String subject) throws Exception {
        sendEmail(
                name,
                email,
                subject,
                cozeBot.genContent(name, subject));
    }

    public CompletableFuture<Void> sendEmailGenFromBotAsync(String name, String email, String subject, Executor executor) throws Exception {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        return cozeBot.genContent(name, subject);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor)
                .thenAcceptAsync(
                        content -> {
                            try {
                                sendEmail(name, email, subject, content);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }, executor);
    }

    public CompletableFuture<Void> sendEmailGenFromBotAsync(String name, String email, String subject) throws Exception {
        return sendEmailGenFromBotAsync(name, email, subject, executor);
    }
}
