package org.example.holidaymailer.repository;


import org.example.holidaymailer.entity.EmailSendRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmailSendRecordRepository extends JpaRepository<EmailSendRecord, Long> {

    // Find records that are PENDING or FAILED, for the current day, and within retry limits
    List<EmailSendRecord> findByStatusInAndSendDateEqualsAndRetryCountLessThan(List<String> statuses, LocalDate sendDate, int maxRetries);

    // Optional: Find records for a specific employee and date to prevent duplicates if needed
    List<EmailSendRecord> findByEmployeeIdAndSendDate(Long employeeId, LocalDate sendDate);
}
