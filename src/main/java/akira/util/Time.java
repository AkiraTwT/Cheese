package akira.util;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class Time {
    private static final ZoneId SAMARA_ZONE = ZoneId.of("Europe/Samara");
    private static final Locale RU_LOCALE = Locale.of("ru", "RUS");

    public static int getWeekNum() {
        LocalDate currentDate = LocalDate.now(SAMARA_ZONE);
        LocalDate startOfYear = LocalDate.of(currentDate.getYear(), 1, 1);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeek = currentDate.get(weekFields.weekOfYear());
        int startWeek = startOfYear.get(weekFields.weekOfYear());
        return currentWeek - startWeek + 209;
    }

    @NotNull
    public static String getCurrentDay(int plus) {
        LocalDateTime currentTime = LocalDateTime.now(SAMARA_ZONE);
        String nextDay = currentTime.plusDays(plus).getDayOfWeek().getDisplayName(TextStyle.FULL, RU_LOCALE);
        return nextDay.substring(0, 1).toUpperCase() + nextDay.substring(1);
    }
}