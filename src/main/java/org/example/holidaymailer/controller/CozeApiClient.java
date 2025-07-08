package org.example.holidaymailer.controller;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.example.holidaymailer.entity.EmailLog;
import org.example.holidaymailer.repository.EmailLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class CozeApiClient {

    @Autowired
    EmailLogRepository emailLogRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MeterRegistry meterRegistry;

    private final WebClient webClient;

    private final String apiUrl;

    private final String token;

    private final String workFlowId;

    public CozeApiClient(@Value("${coze.api.apiUrl}") String apiUrl,
                         @Value("${coze.api.token}") String token,
                         @Value("${coze.api.workFlowId}") String workFlowId) {
        this.apiUrl = apiUrl;
        this.token = token;
        this.workFlowId = workFlowId;

        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", token)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public static CozeApiClient linked(String apiUrl, String token, String workFlowId) {
        return new CozeApiClient(apiUrl, token, workFlowId);
    }

    public Mono<String> callCozeApiSendEmailAsync(String name, String holiday, boolean birth, String email, String subject) {
        // 构建请求体
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("birth", birth);
        parameters.put("holiday", holiday);
        parameters.put("name", name);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("workflow_id", workFlowId);
        requestBody.put("parameters", parameters);

        String taskId = UUID.randomUUID().toString();

        // 发送 POST 请求并处理流式响应
        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class) // 假设响应是 SSE，每行一个字符串
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException)
                        .doBeforeRetry(signal -> log.warn("Retrying AI request: attempt {}", signal.totalRetries() + 1)))
                .doOnSuccess(response -> {
                    // 异步发送邮件
                    sendEmailAsync(taskId, email, subject, response).subscribe();
                    meterRegistry.counter("ai.request.success", "id", taskId).increment();
                })
                .doOnError(error -> {
                    log.error("AI service error for task {}: {}", taskId, error.getMessage());
                    meterRegistry.counter("ai.request.failure", "taskId", taskId).increment();
                })
                .onErrorReturn("Fallback response");

    }

    private Mono<Void> sendEmailAsync(String taskId, String recipient, String subject, String content) {
        // 创建邮件任务
        EmailLog emailTask = new EmailLog();
        emailTask.setTaskId(taskId);
        emailTask.setRecipient(recipient);
        emailTask.setSubject(subject);
        emailTask.setContent(content);
        emailTask.setStatus("PENDING");
        emailTask.setRetryCount(0);
        emailTask.setCreatedAt(LocalDateTime.now());
        emailLogRepository.save(emailTask);

        return Mono.fromRunnable(() -> {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(recipient);
                    message.setSubject(subject);
                    message.setText(content);
                    mailSender.send(message);
                    // 更新任务状态
                    emailTask.setStatus("SENT");
                    emailLogRepository.save(emailTask);
                    meterRegistry.counter("email.send.success", "taskId", taskId).increment();
                })
                .subscribeOn(Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof MailException)
                        .doBeforeRetry(signal -> {
                            log.warn("Retrying email send for task {}: attempt {}", taskId, signal.totalRetries() + 1);
                            emailTask.setRetryCount(emailTask.getRetryCount() + 1);
                            emailLogRepository.save(emailTask);
                        }))
                .doOnError(error -> {
                    log.error("Failed to send email for task {} after retries: {}", taskId, error.getMessage());
                    emailTask.setStatus("FAILED");
                    emailLogRepository.save(emailTask);
                    meterRegistry.counter("email.send.failure", "taskId", taskId).increment();
                }).then();
    }
}
