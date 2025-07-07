package org.example.holidaymailer;

import org.example.holidaymailer.entity.DayType;
import org.example.holidaymailer.entity.Employee;
import org.example.holidaymailer.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class BirthdayMailTask {

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 8 * * *") // 每天早上8点检查生日
    public void sendBirthdayGreetings() {
        LocalDate today = LocalDate.now();
        List<Employee> employees = employeeRepo.findAll();

        employees.parallelStream()
                .filter(employee -> employee.isBirthday(today))
                .forEach(employee -> {
                    emailService.sendMail(employee.getName(), DayType.Birthday.toString());
                });
    }
}

