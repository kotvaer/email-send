package org.example.holidaymailer.service;

import org.example.holidaymailer.config.CozeBot;
import org.example.holidaymailer.entity.EmailMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    private CompletableFuture<Void> sendEmailAsync(String name, String email, String subject, String content) throws Exception {
        return CompletableFuture.runAsync(() -> {
            try {
                sendEmail(name, email, subject, content);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }


    private CompletableFuture<Void> sendEmailGenFromBotAsync(String name, String email, String subject, Executor executor) throws Exception {
        return cozeBot.genContentAsync(name, subject)
                .thenCompose(content -> {
                    try {
                        return sendEmailAsync(name, email, subject, content);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public CompletableFuture<Void> sendEmailGenFromBotAsync(String name, String email, String subject) throws Exception {
        return sendEmailGenFromBotAsync(name, email, subject, executor);
    }


    // 新增 Reactor Mono 异步方法，基于 CozeBot 的 callReactive() 和 WebClient
    public Mono<Void> sendEmailGenFromBotReactive(String name, String email, String subject) throws Exception {
        return cozeBot.genContentReactive(name, subject)
                .flatMap(content ->
                {
                    try {
                        return Mono.fromFuture(sendEmailAsync(name, email, subject, content));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException(e));
                    }
                });
    }
}
