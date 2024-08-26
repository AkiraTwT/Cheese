package akira.command;

import akira.Bot;
import akira.db.Core;
import akira.listener.TimeCheckScheduler;
import akira.util.*;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class CommandHandler extends ListenerAdapter {
    @Setter
    private static TextChannel adminChannel;
    @Getter @Setter
    private static TextChannel defaultChannel;
    private static final Core core = Bot.getCore();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "расписание" -> {
                event.deferReply(true).queue();
                String day = event.getOption("день").getAsString();
                List<Day> lessons = ScheduleParser.printScheduleForDay(day);
                event.getHook().sendMessageEmbeds(toEmbed(lessons)).queue();
            }
            case "сегодня" -> {
                event.deferReply(true).queue();
                List<Day> days = ScheduleParser.printScheduleForDay(Time.getCurrentDay(0));
                event.getHook().sendMessageEmbeds(toEmbed(days)).queue();
            }
            case "пропуски" -> {
                String userId = event.getUser().getId();
                List<User> userList = core.getUserById(userId);
                event.replyEmbeds(userEmbed(userList)).setEphemeral(true).queue();
            }
            case "week-range" -> event.reply(ScheduleParser.parseWeekDateRange()).queue();
            case "set-current-week" -> {
                Link.setCurrentWeek();
                event.reply("Выставлена текущая неделя").queue();
            }
            case "change-week" -> event.reply("Текущая неделя " + ScheduleParser.parseWeekDateRange() + ". Сменить неделю?")
                    .addActionRow(
                            Button.primary("back", "Прошлая неделя"),
                            Button.primary("next", "Следующая неделя")
                    ).queue();
            case "adduser" -> {
                String userId = event.getOption("id").getAsString();
                String surname = event.getOption("surname").getAsString();
                boolean success = core.addUser(userId, surname);
                adminChannel.sendMessage("Пользователь " + surname + (success ? " добавлен" : " не удалось добавить")).queue();
                event.reply("Сообщение перенаправлено").setEphemeral(true).queue();
            }
            case "users" -> {
                List<User> userList = core.getAllUsers();
                event.replyEmbeds(allUserEmbed(userList)).queue();
            }
            case "update-absences" -> {
                String surname = event.getOption("surname").getAsString();
                int absences = event.getOption("absences").getAsInt();
                boolean success = core.updateAbsences(surname, absences);
                adminChannel.sendMessage("НБ у пользователя " + surname + (success ? " обновлен" : " не удалось обновить")).queue();
                event.reply("Сообщение перенаправлено").setEphemeral(true).queue();
            }
            case "update-excused-absences" -> {
                String surname = event.getOption("surname").getAsString();
                int excusedAbsences = event.getOption("excused_absences").getAsInt();
                boolean success = core.updateExcusedAbsences(surname, excusedAbsences);
                adminChannel.sendMessage("УП у пользователя " + surname + (success ? " обновлен" : " не удалось обновить")).queue();
                event.reply("Сообщение перенаправлено").setEphemeral(true).queue();
            }
            case "update-id" -> {
                String newId = event.getOption("id").getAsString();
                String surname = event.getOption("surname").getAsString();
                boolean success = core.updateId(newId, surname);
                adminChannel.sendMessage("ID у пользователя " + surname + (success ? " обновлен" : " не удалось обновить")).queue();
                event.reply("Сообщение перенаправлено").setEphemeral(true).queue();
            }
            case "rename" -> {
                String oldSurname = event.getOption("old").getAsString();
                String newSurname = event.getOption("new").getAsString();
                boolean success = core.updateSurname(oldSurname, newSurname);
                adminChannel.sendMessage("Фамилия пользователя " + oldSurname + (success ? " обновлена на " + newSurname : " не удалось обновить")).queue();
                event.reply("Сообщение перенаправлено").setEphemeral(true).queue();
            }
            case "rm" -> {
                String userId = event.getOption("id").getAsString();
                boolean success = core.deleteUser(userId);
                adminChannel.sendMessage("Пользователь " + "с id " + userId +  (success ? " удален" : " не удалось удалить")).queue();
                event.reply("Сообщение перенаправлено").setEphemeral(true).queue();
            }
            case "channel" -> {
                boolean isAdminChannel = event.getOption("admin").getAsBoolean();
                TextChannel channel = event.getOption("channel").getAsChannel().asTextChannel();
                if (isAdminChannel) {
                    adminChannel = channel;
                    updateAdminChannelId(channel);
                } else {
                    defaultChannel = channel;
                    updateDefaultChannelId(channel);
                }
                event.reply("Канал обновлен!").queue();
            }
            case "url" -> {
                String url = event.getOption("url").getAsString();
                Config.update("url", url);
                Config.update("base_url", Link.getBaseUrl(url));
                event.reply("URL обновлен!").queue();
            }
            case "time" -> {
                int hour = event.getOption("hours").getAsInt();
                int minute = event.getOption("minutes").getAsInt();
                TimeCheckScheduler.setHour(hour);
                TimeCheckScheduler.setMinute(minute);
                Config.update("time.hour", hour);
                Config.update("time.minute", minute);
                event.reply("Время для рассылки обновлено на " + hour + ":" + minute).queue();
            }
        }
    }

    private void updateDefaultChannelId(TextChannel channel) {
        Config.update("channel.default", channel.getId());
    }

    private void updateAdminChannelId(TextChannel channel) {
        Config.update("channel.admin", channel.getId());
    }

    private MessageEmbed toEmbed(List<Day> lessons) {
        String day = ScheduleParser.getCurrentDay();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED);
        builder.setTitle("Расписание на " + (day == null ? "выбранный день" : day));

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

    private MessageEmbed allUserEmbed(List<User> userList) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED);

        for (User user : userList) {
            String userDetails = "\n**Surname**: " + user.getSurname() +
                    "\n**НБ**: " + user.getAbsences() +
                    "\n**УП**: " + user.getExcusedAbsences();

            builder.addField("**ID**: " + user.getDiscordId(), userDetails, true);
        }
        return builder.build();
    }

    private MessageEmbed userEmbed(List<    User> userList) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED);
        builder.setTitle("Данные об НБ/УП");

        for (User user : userList) {
            String userDetails = "**" + user.getSurname() + "**\n" +
                    "**НБ**: " + user.getAbsences() + "\n" +
                    "**УП**: " + user.getExcusedAbsences();

            builder.setDescription(userDetails);
        }
        return builder.build();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        handleWeekChange(event, componentId.equals("back"));
    }

    private void handleWeekChange(ButtonInteractionEvent event, boolean isPrevious) {
        if (isPrevious) {
            Link.setPreviousWeek();
        } else {
            Link.setNextWeek();
        }

        ActionRow row = ActionRow.of(
                Button.secondary("back", "Прошлая неделя").asDisabled(),
                Button.secondary("next", "Следующая неделя").asDisabled());

        event.getMessage().editMessageComponents(row).queue();
        event.reply("Расписание переведено на " + (isPrevious ? "прошлую" : "следующую") + " неделю " + ScheduleParser.parseWeekDateRange()).queue();
    }
}