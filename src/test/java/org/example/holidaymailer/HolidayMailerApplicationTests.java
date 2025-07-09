package org.example.holidaymailer;

import com.coze.openapi.client.workflows.run.RunWorkflowResp;
import org.example.holidaymailer.config.CozeBot;
import org.example.holidaymailer.entity.EmailMessage;
import org.example.holidaymailer.repository.EmployeeRepository;
import org.example.holidaymailer.service.EmailService;
import org.example.holidaymailer.tools.DateTools;
import org.example.holidaymailer.tools.JsonParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@SpringBootTest
class HolidayMailerApplicationTests {
    @Autowired
    private EmailService emailService;


    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    CozeBot cozeBot;

    @Autowired
    JsonParser jsonParser;

    @Test
    void sendEmail() {
        emailService.sendEmail(
                EmailMessage.of("epiiplus@outlook.com",
                        "节日快乐！🎉",
                        "亲爱的同事，今天是法定节假日，祝您节日快乐、阖家幸福！")
        );
    }

    @Test
    void contextLoadsDb() {
        employeeRepository.findAllEmails().forEach(System.out::println);
    }

    @Test
    void contextLoadsDbProj() {
        employeeRepository.findAllNameEmails().forEach(System.out::println);
    }


    @Test
    void dateTools(){
        DateTools.haveItOrNot(LocalDate.of(2025,10,6)).ifPresent(System.out::println);
        DateTools.haveItOrNot(LocalDate.of(2025,10,1)).ifPresent(System.out::println);
    }

    @Test
    void cozeBotTest() throws Exception {
        CompletableFuture<RunWorkflowResp> respCompletableFuture = cozeBot.callAsync("shy", "生日");
        RunWorkflowResp resp = respCompletableFuture.get();
        System.out.println(resp.getData());
        System.out.println(jsonParser.getMessageFromJson(resp.getData()));
    }

    @Test
    void cozeBotTestSync() throws Exception {
        emailService.sendEmailGenFromBotAsync(
                "shy",
                "epiiplus@outlook.com",
                "生日快乐!").join();
    }

    @Test
    void cozeBotTestASync() throws Exception {
        emailService.sendEmailGenFromBotAsync(
                "shy",
                "epiiplus@outlook.com",
                "上班快乐🎉!").join();
    }

    @Test
    void cozeBotTestReactive() throws Exception {
        emailService.sendEmailGenFromBotReactive(
                "shy",
                "epiiplus@outlook.com",
                "暑假快乐🎉!").block();
    }

    @Test
    void cozeBotASync() throws Exception {
        RunWorkflowResp block = cozeBot.callReactive("shy", "春节").block();
        System.out.println(block.getData());
    }


}
