package org.example.holidaymailer.tools;

import org.example.holidaymailer.controller.CozeApiClient;
import org.example.holidaymailer.entity.Employee;
import org.example.holidaymailer.repository.EmployeeRepository;
import org.example.holidaymailer.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Autowired
    CozeApiClient cozeApiClient;

    @Scheduled(cron = "0 0 8 * * *") // 定时任务
    public void sendBirthdayGreetings() {
        LocalDate today = LocalDate.now();

        // 根据当天日期获取当天生日的员工列表
        List<Employee> employees = employeeRepo.findByBirthdayMonthAndDay(
                today.getMonthValue(),
                today.getDayOfMonth());


        employees.parallelStream()
                .forEach(employee -> {
                    cozeApiClient.callCozeApiSendEmailAsync(employee.getName(),
                            null,
                            true,
                            employee.getEmail(),
                            "生日快乐");
                });
    }


    @Scheduled(cron = "0 8 14 * * *") // 定时任务执行
    public void sendHolidayGreeting() {
        Optional<String> opts = DateTools.haveItOrNot(LocalDate.now());
        if (opts.isEmpty()) return;

        LocalDate today = LocalDate.now();
        List<String> emails = employeeRepo.findAllEmails();

        emails.parallelStream()
                .forEach(email -> {
                    cozeApiClient.callCozeApiSendEmailAsync(null,
                            opts.get(),
                            false,
                            email,
                            opts.get());
                });
    }
}
