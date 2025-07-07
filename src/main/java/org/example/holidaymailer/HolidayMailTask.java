package org.example.holidaymailer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class HolidayMailTask {

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 8 14 * * *") // 每天早上9点执行
    public void sendHolidayGreeting() {
        LocalDate today = LocalDate.now();
        if (holidayService.isHoliday(today)) {
            List<String> emails = holidayService.getAllEmployeeEmails();
            for (String email : emails) {
                try {
                    emailService.sendMail(
                            email,
                            "节日快乐！🎉",
                            "亲爱的同事，今天是法定节假日，祝您节日快乐、阖家幸福！"
                    );
                    System.out.println("✅ 已发送：" + email);
                } catch (Exception e) {
                    System.err.println("❌ 发送失败：" + email + "，原因：" + e.getMessage());
                }
            }
        }
    }
}

