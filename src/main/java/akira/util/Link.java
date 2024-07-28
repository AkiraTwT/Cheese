package akira.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Link {
    public static void setNextWeek() {
        String currentUrlWeek = Config.getString("url");
        Config.update("url", modifyWeek(currentUrlWeek, 1));
    }

    public static void setPreviousWeek() {
        String currentUrlWeek = Config.getString("url");
        Config.update("url", modifyWeek(currentUrlWeek, -1));
    }

    public static void setCurrentWeek() {
        Config.update("url", Config.getString("base_url") + Time.getWeekNum());
    }

    public static String getBaseUrl(String url) {
        String delimiter = "&wk=";
        int index = url.indexOf(delimiter);
        return url.substring(0, index + delimiter.length());
    }

    private static String modifyWeek(String url, int weekData) {
        Pattern pattern = Pattern.compile("([&?]wk=)(\\d+)");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            int currentWeek = Integer.parseInt(matcher.group(2));
            int newWeek = currentWeek + weekData;
            return matcher.replaceFirst(matcher.group(1) + newWeek);
        } else {
            throw new IllegalArgumentException("URL does not contain 'wk' parameter");
        }
    }
}