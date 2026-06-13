package org.example.holidaymailer.entity;

public record EmailMessage(String to, String subject, String content) {
    public static EmailMessage of(String to, String subject, String content) {
        return new EmailMessage(to, subject, content);
    }
}
