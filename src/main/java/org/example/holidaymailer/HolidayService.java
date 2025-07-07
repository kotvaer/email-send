package org.example.holidaymailer;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class HolidayService {

    private final Set<LocalDate> holidays = Set.of(
            LocalDate.of(2025,7,7),
            LocalDate.of(2025, 1, 1),   // 元旦
            LocalDate.of(2025, 2, 1),   // 春节示例
            LocalDate.of(2025, 5, 1)    // 劳动节
    );

    public boolean isHoliday(LocalDate date) {
        return holidays.contains(date);
    }

    public List<String> getAllEmployeeEmails() {
        return List.of(
                "epiiplus@outlook.com"
        );
    }
}
