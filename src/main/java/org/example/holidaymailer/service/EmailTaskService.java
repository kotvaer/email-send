package org.example.holidaymailer.service;


import org.example.holidaymailer.config.CozeBot;
import org.example.holidaymailer.entity.EmailSendRecord;
import org.example.holidaymailer.repository.EmailSendRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailTaskService {

    private static final Logger log = LoggerFactory.getLogger(EmailTaskService.class);

    @Autowired
    private EmailSendRecordRepository emailSendRecordRepository;

    @Autowired
    private CozeBot cozeBot;

    @Autowired
    private EmailService emailService;

    // 最大重试次数
    private static final int MAX_RETRIES = 3;

    /**
     * 处理单个邮件发送任务的完整流程：生成内容 -> 发送邮件 -> 更新状态。
     * 使用 @Async 确保这个方法在单独的线程中执行，不阻塞调度器。
     * @param record 待处理的邮件发送记录
     */
    @Async
    @Transactional // 确保数据库操作的原子性
    public CompletableFuture<Void> processEmailSendRecord(EmailSendRecord record) throws Exception {
        log.info("开始处理邮件记录 ID: {}, 员工: {}", record.getId(), record.getEmployeeName());

        // 标记为发送中，防止重复处理
        record.setStatus("SENDING");
        record.setLastAttemptTime(LocalDateTime.now());
        emailSendRecordRepository.save(record);

        // 1. 调用 CozeBot 生成邮件内容
        return cozeBot.genContentAsync(record.getEmployeeName(), record.getSubject())
                .thenCompose(content -> {
                    // 内容生成成功，更新记录并存储内容
                    record.setContent(content);
                    emailSendRecordRepository.save(record);
                    log.info("邮件记录 ID: {}，内容已生成。", record.getId());

                    // 2. 调用 EmailService 发送邮件
                    return emailService.sendEmailAsync(
                            record.getEmployeeName(),
                            record.getEmployeeEmail(),
                            record.getSubject(),
                            record.getContent()
                    );
                })
                .thenAccept(v -> {
                    // 邮件发送成功
                    record.setStatus("SENT");
                    record.setSentTime(LocalDateTime.now());
                    record.setErrorMessage(null); // 清除之前的错误信息
                    emailSendRecordRepository.save(record);
                    log.info("邮件记录 ID: {}，发送成功。", record.getId());
                })
                .exceptionally(ex -> {
                    // 邮件内容生成或发送过程中发生异常
                    record.setRetryCount(record.getRetryCount() + 1);
                    record.setErrorMessage(ex.getMessage());

                    if (record.getRetryCount() >= MAX_RETRIES) {
                        record.setStatus("FAILED"); // 达到最大重试次数，标记为最终失败
                        log.error("邮件记录 ID: {} 达到最大重试次数，最终失败: {}", record.getId(), ex.getMessage());
                    } else {
                        record.setStatus("PENDING"); // 标记为PENDING以便下次重试
                        log.warn("邮件记录 ID: {} 发送失败，第 {} 次重试。错误: {}", record.getId(), record.getRetryCount(), ex.getMessage());
                    }
                    emailSendRecordRepository.save(record); // 保存更新后的状态
                    return null; // 返回null表示异常已被处理，CompletableFuture不会传播异常
                });
    }
}
