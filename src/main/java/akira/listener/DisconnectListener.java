package akira.listener;

import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisconnectListener extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DisconnectListener.class);

    @Override
    public void onSessionDisconnect(@NotNull SessionDisconnectEvent event) {
        LOGGER.warn("Bot disconnected from Discord at {}! State: {}", event.getTimeDisconnected(), event.getState());
    }
}