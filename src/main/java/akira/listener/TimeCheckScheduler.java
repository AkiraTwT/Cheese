package akira.listener;

import akira.command.CommandHandler;
import akira.util.*;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeCheckScheduler extends ListenerAdapter {
    @Setter
    private static int hour;
    @Setter
    private static int minute;
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeCheckScheduler.class);

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        hour = getHourFromConfig();
        minute = getMinuteFromConfig();
        CommandHandler.setAdminChannel(event.getJDA().getTextChannelById(getAdminChannelId()));
        CommandHandler.setDefaultChannel(event.getJDA().getTextChannelById(getDefaultChannelId()));

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(createWeeklyTask(), 0, Duration.ofHours(12).toMillis());
        timer.scheduleAtFixedRate(createDailyTask(), 0, Duration.ofMinutes(1).toMillis());
    }

    private TimerTask createDailyTask() {
        return new TimerTask() {
            @Override
            public void run() {
                LocalDateTime currentTime = LocalDateTime.now();

                if (currentTime.getHour() == hour && currentTime.getMinute() == minute) {
                    List<Day> days = ScheduleParser.printScheduleForDay(Time.getCurrentDay(1));
                    TextChannel channel = CommandHandler.getDefaultChannel();
                    channel.sendMessageEmbeds(toEmbed(days)).queue();
                }
            }
        };
    }

    private TimerTask createWeeklyTask() {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    String dateRange = ScheduleParser.parseWeekDateRange();
                    LocalDate currentDate = LocalDate.now();

                    int scheduleDay = extractDay(dateRange);
                    int scheduleMonth = extractMonth(dateRange);

                    LocalDate scheduleDate = LocalDate.of(currentDate.getYear(), scheduleMonth, scheduleDay);

                    if (currentDate.isAfter(scheduleDate)) {
                        Link.setNextWeek();
                    }
                } catch (RuntimeException e) {
                    LOGGER.error("Failed to update schedule link: {}", e.getMessage());
                }
            }
        };
    }

    private int getHourFromConfig() {
        return Config.getInteger("time.hour");
    }

    private int getMinuteFromConfig() {
        return Config.getInteger("time.minute");
    }

    private String getAdminChannelId() {
        return Config.getString("channel.admin");
    }

    private String getDefaultChannelId() {
        return Config.getString("channel.default");
    }

    private MessageEmbed toEmbed(List<Day> lessons) {
        String day = ScheduleParser.getCurrentDay();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED);
        builder.setTitle("Расписание на " + (day == null ? "Завтра" : day));

        if (lessons.isEmpty()) {
            builder.setDescription("Расписания нет");
        } else {
            for (Day lesson : lessons) {
                StringBuilder lessonDetails = new StringBuilder();
                lessonDetails.append("**Время**: ").append(lesson.getTime());

                if (!lesson.getTeacher().isEmpty()) {
                    lessonDetails.append("\n**Преподаватель**: ").append(lesson.getTeacher());
                }
                if (!lesson.getTask().isEmpty()) {
                    lessonDetails.append("\n**Задание**: ").append(lesson.getTask());
                }
                if (!lesson.getResource().isEmpty()) {
                    lessonDetails.append("\n**Ресурс**: ").append(lesson.getResource());
                }
                if (!lesson.getTopic().isEmpty()) {
                    lessonDetails.append("\n**Тема**: ").append(lesson.getTopic());
                }
                if (!lesson.getMethod().isEmpty()) {
                    lessonDetails.append("\n**Метод**: ").append(lesson.getMethod());
                }
                if (!lesson.getLocation().isEmpty()) {
                    lessonDetails.append("\n**Адрес**: ").append(lesson.getLocation());
                }
                if (!lesson.getCabinet().isEmpty()) {
                    lessonDetails.append("\n**Кабинет**: ").append(lesson.getCabinet());
                }

                builder.addField("**" + lesson.getNumber() + "**: " + lesson.getDiscipline(), lessonDetails.toString(), true);
            }
        }
        return builder.build();
    }

    private int extractDay(String dateRange) {
        Pattern pattern = Pattern.compile("по\\s(\\d{2})\\.\\d{2}\\.\\d{4}");
        Matcher matcher = pattern.matcher(dateRange);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            throw new IllegalArgumentException("Invalid date range format: " + dateRange);
        }
    }

    private int extractMonth(String dateRange) {
        Pattern pattern = Pattern.compile("по\\s\\d{2}\\.(\\d{2})\\.\\d{4}");
        Matcher matcher = pattern.matcher(dateRange);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            throw new IllegalArgumentException("Invalid date range format: " + dateRange);
        }
    }
}