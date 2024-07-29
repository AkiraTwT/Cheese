package akira.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Time {
    private static final ZoneId samaraZone = ZoneId.of("Europe/Samara");
    private static final Map<String, String> dayOfWeekTranslations = new HashMap<>();

    static {
        dayOfWeekTranslations.put("Monday", "Понедельник");
        dayOfWeekTranslations.put("Tuesday", "Вторник");
        dayOfWeekTranslations.put("Wednesday", "Среда");
        dayOfWeekTranslations.put("Thursday", "Четверг");
        dayOfWeekTranslations.put("Friday", "Пятница");
        dayOfWeekTranslations.put("Saturday", "Суббота");
        dayOfWeekTranslations.put("Sunday", "Воскресенье");
    }

    public static int getWeekNum() {
        LocalDate currentDate = LocalDate.now(samaraZone);
        LocalDate startOfYear = LocalDate.of(currentDate.getYear(), 1, 1);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeek = currentDate.get(weekFields.weekOfYear());
        int startWeek = startOfYear.get(weekFields.weekOfYear());
        return currentWeek - startWeek + 209;
    }

    public static String getCurrentDay(int plus) {
        LocalDateTime currentTime = LocalDateTime.now();
        DayOfWeek nextDay = currentTime.getDayOfWeek().plus(plus);
        String dayOfWeekEnglish = nextDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String dayOfWeekRussian = dayOfWeekTranslations.get(dayOfWeekEnglish);
        return dayOfWeekRussian != null ? dayOfWeekRussian : "Неизвестный день";
    }
}