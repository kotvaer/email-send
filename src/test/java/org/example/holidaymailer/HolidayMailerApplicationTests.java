package org.example.holidaymailer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HolidayMailerApplicationTests {
    @Autowired
    private EmailService emailService;

    @Test
    void contextLoads() {
        emailService.sendMail(
                "epiiplus@outlook.com",
                "节日快乐！🎉",
                "亲爱的同事，今天是法定节假日，祝您节日快乐、阖家幸福！"
        );
    }
}
