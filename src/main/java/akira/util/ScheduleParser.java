package akira.util;

import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScheduleParser {
    @Getter
    private static String currentDay;
    private static String URL;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleParser.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0";

    private static String loadHtmlSnapshot(String url) {
        String html = null;
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .referrer("http://www.google.com")
                    .get();
            html = doc.html();
        } catch (IOException e) {
            LOGGER.error("Failed to load HTML snapshot from URL: {}", url, e);
        }
        return html;
    }

    public static List<Day> printScheduleForDay(String day) {
        List<Day> schedule = new ArrayList<>();
        URL = Config.getString("url");
        Document doc = Jsoup.parse(loadHtmlSnapshot(URL));
        Elements tables = doc.select("table");
        boolean isCorrectDay = false;
        currentDay = "Сегодня";

        for (Element table : tables) {
            Elements rows = table.select("tr");

            for (Element row : rows) {
                if (row.text().contains(day)) {
                    isCorrectDay = true;
                    currentDay = row.text().split(" / ")[0];
                    continue;
                }

                if (isCorrectDay) {
                    Elements cells = row.select("td");

                    if (cells.getFirst().text().equals("№ пары")) {
                        continue;
                    }

                    if (cells.size() > 1) {
                        Day dayEntry = new Day();
                        dayEntry.setNumber(cells.get(0).text());
                        parseTime(cells.get(1), dayEntry);
                        dayEntry.setMethod(cells.get(2).text());
                        parseDisciplineAndTeacher(cells.get(3), dayEntry);
                        dayEntry.setTopic(cells.get(4).text().replaceAll("Тема ", ""));
                        dayEntry.setResource(cells.get(5).text());
                        dayEntry.setTask(cells.get(6).text());
                        schedule.add(dayEntry);
                    } else {
                        if (row.text().isEmpty()) {
                            break;
                        }
                    }
                }
            }
            if (isCorrectDay) {
                break;
            }
        }
        return schedule;
    }

    public static String parseWeekDateRange() {
        URL = Config.getString("url");
        try {
            Document doc = Jsoup.connect(URL)
                    .userAgent(USER_AGENT)
                    .referrer("http://www.google.com")
                    .get();
            return doc.select("body > table:nth-child(4) > tbody > tr:nth-child(3) > td").text();
        } catch (IOException e) {
            LOGGER.error("Error occurred while fetching data from URL: {}", URL, e);
            return null;
        }
    }

    private static void parseTime(Element cell, Day dayEntry) {
        if (cell.text().contains("дистанционно")) {
            dayEntry.setTime(cell.text());
        } else {
            cell.select(".t_zm").remove();
            dayEntry.setTime(cell.text());
        }
    }

    private static void parseDisciplineAndTeacher(Element cell, Day dayEntry) {
        boolean hasReplacement = cell.html().contains("Замена");

        if (hasReplacement) {
            Elements zmElements = cell.select(".t_zm");
            for (Element zmElement : zmElements) {
                zmElement.remove();
            }
        }

        String[] lines = cell.html()
                .replaceAll("<font class=\"t_green_10\">|</font>", "")
                .replaceAll("<i>|</i>", "")
                .replaceAll("</?a[^>]*>", "")
                .replaceAll("Кабинет: ", "")
                .replace("\n", "")
                .split("<br>");

        int offset = hasReplacement ? 1 : 0;

        if (lines.length > offset) {
            dayEntry.setDiscipline(lines[offset].trim());
        } else {
            dayEntry.setDiscipline("");
        }
        if (lines.length > offset + 1) {
            dayEntry.setTeacher(lines[offset + 1].trim());
        } else {
            dayEntry.setTeacher("");
        }
        if (lines.length > offset + 2) {
            dayEntry.setLocation(lines[offset + 2].trim());
        } else {
            dayEntry.setLocation("");
        }
        if (lines.length > offset + 3) {
            dayEntry.setCabinet(lines[offset + 3].trim());
        } else {
            dayEntry.setCabinet("");
        }
    }
}