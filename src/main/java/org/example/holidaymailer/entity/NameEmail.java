package org.example.holidaymailer.entity;


public interface NameEmail {
    Long getId();
    String getName();
    String getEmail();

    default EmailSendRecord toEmailSendRecord(String subject){
        return new EmailSendRecord(
                getId(),
                getName(),
                getEmail(),
                subject
        );
    }
}
