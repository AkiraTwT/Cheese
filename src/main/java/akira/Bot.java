package akira;

import akira.command.CommandHandler;
import akira.db.Core;
import akira.listener.DisconnectListener;
import akira.listener.TimeCheckScheduler;
import akira.util.Config;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Bot extends ListenerAdapter {
    @Getter
    private static JDA jda;
    @Getter
    private static Core core;
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);

    public static void main(String[] args) {
        core = new Core();
        Config.load();

        jda = JDABuilder.createDefault(Config.getString("bot.token"))
                .setActivity(Activity.watching("расписание"))
                .setStatus(OnlineStatus.ONLINE)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_PRESENCES)
                .addEventListeners(
                        new Bot(),
                        new CommandHandler(),
                        new TimeCheckScheduler(),
                        new DisconnectListener())
                .build();
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            LOGGER.error("Bot launch error: ", e);
        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        commandData.add(Commands.slash("расписание", "Показать расписание на выбранный день")
                .setGuildOnly(true)
                .addOptions(new OptionData(OptionType.STRING, "день", "Выберите день для просмотра расписания", true)
                        .addChoice("понедельник", "Понедельник")
                        .addChoice("вторник", "Вторник")
                        .addChoice("среда", "Среда")
                        .addChoice("четверг", "Четверг")
                        .addChoice("пятница", "Пятница")
                        .addChoice("суббота", "Суббота")));

        commandData.add(Commands.slash("сегодня", "Показать расписание на сегодня")
                .setGuildOnly(true));

        commandData.add(Commands.slash("пропуски", "Показать данные о пропусках")
                .setGuildOnly(true));

        commandData.add(Commands.slash("week-range", "Показать начальную и конечную даты текущей недели")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        commandData.add(Commands.slash("change-week", "Изменить текущую неделю")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        commandData.add(Commands.slash("set-current-week", "Установить текущую неделю")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        commandData.add(Commands.slash("adduser", "Добавить нового пользователя")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addOptions(new OptionData(OptionType.STRING, "id", "ID пользователя", true))
                .addOptions(new OptionData(OptionType.STRING, "surname", "Фамилия", true)));

        commandData.add(Commands.slash("users", "Показать всех пользователей")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        commandData.add(Commands.slash("update-absences", "Обновить количество НБ у пользователя")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addOptions(new OptionData(OptionType.STRING, "surname", "Фамилия", true))
                .addOptions(new OptionData(OptionType.INTEGER, "absences", "Количество НБ", true)));

        commandData.add(Commands.slash("update-excused-absences", "Обновить количество УП у пользователя")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addOptions(new OptionData(OptionType.STRING, "surname", "Фамилия", true))
                .addOptions(new OptionData(OptionType.INTEGER, "excused_absences", "Количество УП", true)));

        commandData.add(Commands.slash("update-id", "Обновить ID пользователя")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addOptions(new OptionData(OptionType.STRING, "id", "Новое ID", true))
                .addOptions(new OptionData(OptionType.STRING, "surname", "Фамилия", true)));

        commandData.add(Commands.slash("rename", "Обновить фамилию пользователя")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addOptions(new OptionData(OptionType.STRING, "old", "Старая фамилия", true))
                .addOptions(new OptionData(OptionType.STRING, "new", "Новая фамилия", true)));

        commandData.add(Commands.slash("rm", "Удалить пользователя по ID")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addOptions(new OptionData(OptionType.STRING, "id", "ID пользователя", true)));

        commandData.add(Commands.slash("channel", "Установить канал для авто-сообщений")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addOptions(new OptionData(OptionType.BOOLEAN, "admin", "Установить канал для админ сообщений", true))
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Канал", true).setChannelTypes(ChannelType.TEXT)));

        commandData.add(Commands.slash("url", "Изменить URL группы")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addOptions(new OptionData(OptionType.STRING, "url", "Требуется полный URL!", true)));

        commandData.add(Commands.slash("time", "Установить время для автоматической рассылки")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addOptions(new OptionData(OptionType.INTEGER, "hours", "Часы в интервале 0-23", true).setMinValue(0).setMaxValue(23))
                .addOptions(new OptionData(OptionType.INTEGER, "minutes", "Минуты в интервале 0-59", true).setMinValue(0).setMaxValue(59)));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}