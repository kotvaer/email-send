package org.example.holidaymailer.tools;

import org.example.holidaymailer.entity.NameEmail;
import org.example.holidaymailer.repository.EmployeeRepository;
import org.example.holidaymailer.service.EmailService;
import org.example.holidaymailer.service.EmailTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class MailTaskWithRetry {

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private EmailTaskService emailTaskService;

    @Scheduled(cron = "0 0 8 * * *") // 定时任务
    public void sendBirthdayGreetings() {
        LocalDate today = LocalDate.now();

        // 根据当天日期获取当天生日的员工列表
        List<NameEmail> nameEmails = employeeRepo.findNameEmailByBirthday(
                today.getMonthValue(),
                today.getDayOfMonth());

        if (nameEmails.isEmpty()) {
            return;
        }

        String subject = "生日快乐！";

        nameEmails.parallelStream()
                .map(nameEmail -> nameEmail.toEmailSendRecord(subject))
                .forEach(emailSendRecord -> {
                    try {
                        emailTaskService.processEmailSendRecord(emailSendRecord);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    @Scheduled(cron = "0 8 14 * * *") // 定时任务执行
    public void sendHolidayGreeting() {
        Optional<String> opts = DateTools.haveItOrNot(LocalDate.now());
        if (opts.isEmpty()) return;

        List<NameEmail> nameEmails = employeeRepo.findAllNameEmails();

        String subject = opts.get();

        nameEmails.parallelStream()
                .map(nameEmail -> nameEmail.toEmailSendRecord(subject))
                .forEach(emailSendRecord -> {
                    try {
                        emailTaskService.processEmailSendRecord(emailSendRecord);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
