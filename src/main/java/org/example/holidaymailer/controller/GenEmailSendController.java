package org.example.holidaymailer.controller;


import org.example.holidaymailer.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class GenEmailSendController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/send_one")
    public void infoOne(String name, String email, String subject) {
        emailService.sendEmailGenFromBotAsync(name, email, subject);
    }

    @GetMapping("/send_all")
    public void infoAll(String subject) {
        emailService.infoAll(subject);
    }
}
