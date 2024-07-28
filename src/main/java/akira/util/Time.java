package akira.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class Time {
    private static final ZoneId samaraZone = ZoneId.of("Europe/Samara");

    public static int getWeekNum() {
        LocalDate currentDate = LocalDate.now(samaraZone);
        LocalDate startOfYear = LocalDate.of(currentDate.getYear(), 1, 1);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeek = currentDate.get(weekFields.weekOfYear());
        int startWeek = startOfYear.get(weekFields.weekOfYear());
        return currentWeek - startWeek + 209;
    }
}