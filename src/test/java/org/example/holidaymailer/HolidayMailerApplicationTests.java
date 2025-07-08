package org.example.holidaymailer;

import org.example.holidaymailer.controller.CozeApiClient;
import org.example.holidaymailer.entity.EmailMessage;
import org.example.holidaymailer.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HolidayMailerApplicationTests {
    @Autowired
    private EmailService emailService;

    @Autowired
    CozeApiClient cozeApiClient;

    @Test
    void contextLoads() {
        emailService.sendEmail(
                EmailMessage.of("epiiplus@outlook.com",
                        "节日快乐！🎉",
                        "亲爱的同事，今天是法定节假日，祝您节日快乐、阖家幸福！")
        );
    }

    @Test
    void emailServiceTestAsync() {
        cozeApiClient.callCozeApi("shy", "国庆节", false)
                .subscribe(
                        data -> System.out.println("Received: " + data),
                        error -> System.err.println("Error: " + error.getMessage()),
                        () -> System.out.println("Stream completed")
                );
    }

    @Test
    void emailServiceTest() {
        cozeApiClient.callCozeApi("shy", "国庆节", true)
                .doOnNext(data -> System.out.println("Received: " + data))
                .doOnComplete(() -> System.out.println("Stream completed"))
                .doOnError(error -> System.err.println("Error: " + error.getMessage()))
                .blockLast(); // 阻塞直到 Flux 完成
    }

}
