package org.example.holidaymailer.tools;

import org.example.holidaymailer.entity.NameEmail;
import org.example.holidaymailer.repository.EmployeeRepository;
import org.example.holidaymailer.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class MailTask {

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private EmailService emailService;

    //@Scheduled(cron = "0 0 8 * * *") // 定时任务
    public void sendBirthdayGreetings() {
        LocalDate today = LocalDate.now();

        // 根据当天日期获取当天生日的员工列表
        List<NameEmail> nameEmails = employeeRepo.findNameEmailByBirthday(
                today.getMonthValue(),
                today.getDayOfMonth());

        String subject = "生日快乐！";

        emailService.sendAsyncList(nameEmails, subject);
    }


    //@Scheduled(cron = "0 8 14 * * *") // 定时任务执行
    public void sendHolidayGreeting() {
        Optional<String> opts = DateTools.haveItOrNot(LocalDate.now());
        if (opts.isEmpty()) return;

        List<NameEmail> nameEmails = employeeRepo.findAllNameEmails();

        String subject = opts.get();

        emailService.sendAsyncList(nameEmails, subject);

    }
}
