package org.example.holidaymailer.tools;

import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public class DateTools {
    record MonthDay(int month, int day) {
        static MonthDay of(int month, int day) {
            return new MonthDay(month, day);
        }
    }

    private final static Map<MonthDay, String> SOLAR_HOLIDAY = Map.of(
            MonthDay.of(1, 1), "元旦节",
            MonthDay.of(5, 1), "劳动节",
            MonthDay.of(10, 1), "国庆节"
    );

    // 此处略去清明节，这个节日并不适合发祝福
    private final static Map<MonthDay, String> LUNAR_HOLIDAY = Map.of(
            MonthDay.of(12, 29), "除夕",
            MonthDay.of(1, 1), "春节",
            MonthDay.of(8, 15), "中秋节"
    );

    private static MonthDay convertToLunar(LocalDate localDate) {
        Lunar lunar = Solar.fromYmd(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()).getLunar();
        MonthDay.of(lunar.getMonth(), lunar.getDay());
        return MonthDay.of(lunar.getMonth(), lunar.getDay());
    }

    public static Optional<String> haveItOrNot(LocalDate localDate) {
        MonthDay solar = MonthDay.of(localDate.getMonthValue(), localDate.getDayOfMonth());
        MonthDay lunar = convertToLunar(localDate);
        if (SOLAR_HOLIDAY.containsKey(solar)) {
            return Optional.of(SOLAR_HOLIDAY.get(solar));
        } else if (LUNAR_HOLIDAY.containsKey(lunar)) {
            return Optional.of(LUNAR_HOLIDAY.get(lunar));
        } else {
            return Optional.empty();
        }
    }

    public static boolean test(LocalDate localDate) {
        MonthDay date = MonthDay.of(localDate.getMonthValue(), localDate.getDayOfMonth());
        return SOLAR_HOLIDAY.containsKey(date) || LUNAR_HOLIDAY.containsKey(convertToLunar(localDate));
    }


}
