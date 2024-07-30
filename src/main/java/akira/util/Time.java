package akira.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Time {
    private static final ZoneId SAMARA_ZONE = ZoneId.of("Europe/Samara");
    private static final Map<DayOfWeek, String> DAY_OF_WEEK_TRANSLATIONS = new HashMap<>();

    static {
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.MONDAY, "Понедельник");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.TUESDAY, "Вторник");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.WEDNESDAY, "Среда");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.THURSDAY, "Четверг");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.FRIDAY, "Пятница");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.SATURDAY, "Суббота");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.SUNDAY, "Воскресенье");
    }

    public static int getWeekNum() {
        LocalDate currentDate = LocalDate.now(SAMARA_ZONE);
        LocalDate startOfYear = LocalDate.of(currentDate.getYear(), 1, 1);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeek = currentDate.get(weekFields.weekOfYear());
        int startWeek = startOfYear.get(weekFields.weekOfYear());
        return currentWeek - startWeek + 209;
    }

    public static String getCurrentDay(int plus) {
        LocalDateTime currentTime = LocalDateTime.now(SAMARA_ZONE);
        DayOfWeek nextDay = currentTime.plusDays(plus).getDayOfWeek();
        return DAY_OF_WEEK_TRANSLATIONS.getOrDefault(nextDay, "Неизвестный день");
    }
}