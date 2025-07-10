package org.example.holidaymailer.service;

import org.example.holidaymailer.entity.EmailSendRecord;
import org.example.holidaymailer.repository.EmailSendRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class EmailScheduler {

    private static final Logger log = LoggerFactory.getLogger(EmailScheduler.class);

    @Autowired
    private EmailSendRecordRepository emailSendRecordRepository;

    @Autowired
    private EmailTaskService emailTaskService;

    // 最大重试次数，与 EmailTaskService 中的保持一致
    private static final int MAX_RETRIES = 3;

    /**
     * 定时任务：扫描待发送邮件记录并触发发送。
     * 每隔1分钟执行一次（可以根据实际需求调整）。
     * initialDelayMs: 应用启动后第一次执行的延迟时间
     * fixedRateMs: 两次任务执行之间的时间间隔（从上次任务开始算起）
     */
    @Scheduled(initialDelay = 5000, fixedRate = 60000) // 5秒后启动，每60秒执行一次
    public void scanAndSendEmails() throws Exception {
        log.info("定时任务开始扫描待发送邮件... 时间: {}", LocalDateTime.now());

        // 获取今天所有 PENDING 或 FAILED 状态，且重试次数未达上限的邮件记录
        LocalDate today = LocalDate.now();
        List<String> statusesToProcess = Arrays.asList("PENDING", "FAILED");
        List<EmailSendRecord> recordsToProcess =
                emailSendRecordRepository.findByStatusInAndSendDateEqualsAndRetryCountLessThan(
                        statusesToProcess, today, MAX_RETRIES);

        if (recordsToProcess.isEmpty()) {
            log.info("没有找到待发送或待重试的邮件记录。");
            return;
        }

        log.info("找到 {} 条待处理邮件记录。", recordsToProcess.size());

        for (EmailSendRecord record : recordsToProcess) {
            // 为每条记录异步触发处理，不阻塞当前循环
            emailTaskService.processEmailSendRecord(record)
                    .exceptionally(ex -> {
                        // 记录任何未被 EmailTaskService 内部处理的异常
                        log.error("处理邮件记录 ID: {} 时发生未捕获异常: {}", record.getId(), ex.getMessage(), ex);
                        return null;
                    });
        }
        log.info("定时任务完成扫描，已将任务提交进行异步处理。");
    }

    /**
     * 示例：初始数据加载任务
     * 这个任务可以在应用启动后运行一次，用于模拟将原始员工数据转化为邮件发送记录。
     * 实际应用中，这可能是由另一个模块触发，或者在特定节假日前手动运行。
     */
    //@Scheduled(initialDelay = 1000, fixedDelay = Long.MAX_VALUE) // 仅在启动后运行一次
    public void loadInitialEmailRecords() {
        if (emailSendRecordRepository.count() > 0) {
            log.info("数据库中已存在邮件发送记录，跳过初始数据加载。");
            return;
        }

        log.info("开始加载初始邮件发送记录示例...");
        // 模拟一些员工数据
        List<String> employeeNames = Arrays.asList("张三", "李四", "王五", "赵六", "钱七", "孙八");
        LocalDate today = LocalDate.now();
        LocalDate birthdayDate = today.plusDays(1); // 假设明天是生日

        for (int i = 0; i < employeeNames.size(); i++) {
            String name = employeeNames.get(i);
            // 假设一半是生日祝福，一半是节假日祝福
            String emailType = (i % 2 == 0) ? "BIRTHDAY" : "HOLIDAY";
            String subject = (i % 2 == 0) ? "生日快乐！" : "节日快乐！";
            LocalDate targetDate = (i % 2 == 0) ? birthdayDate : today; // 生日祝福是明天，节日祝福是今天

            // 创建邮件发送记录，初始状态为PENDING
            EmailSendRecord record = new EmailSendRecord(
                    (long) (100 + i), // 模拟员工ID
                    name,
                    name.toLowerCase().replace(" ", "") + "@example.com", // 模拟邮箱
                    subject
            );
            emailSendRecordRepository.save(record);
            log.info("添加了邮件记录: {} 给 {}", emailType, name);
        }
        log.info("初始邮件发送记录加载完成。共 {} 条。", employeeNames.size());
    }
}
