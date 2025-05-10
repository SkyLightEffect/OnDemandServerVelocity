package me.skylighteffect.ondemandservervelocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import me.skylighteffect.ondemandservervelocity.configs.MsgCFG;
import me.skylighteffect.ondemandservervelocity.enums.StartingStatus;
import me.skylighteffect.ondemandservervelocity.util.CountdownTask;
import me.skylighteffect.ondemandservervelocity.util.ServerOnDemand;
import net.kyori.adventure.text.Component;

import java.util.concurrent.TimeUnit;

/**
 * Listener for handling pre-connection events to servers.
 * <p>
 * This class intercepts player connection attempts and determines if a target server
 * needs to be started on-demand. If the server is not running, it initiates the startup
 * process and informs the player appropriately via disconnect message or chat.
 * <p>
 * It also schedules a countdown notifier for players waiting for the server to start.
 */
public class ServerConnectListener {

    /**
     * Handles the {@link ServerPreConnectEvent}.
     * <p>
     * If the target server is not currently running, this method attempts to start it.
     * Players are either disconnected with a message or notified in chat, depending on
     * whether they are already connected to a fallback server.
     *
     * @param event The pre-connect event triggered when a player attempts to connect to a server.
     */
    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player p = event.getPlayer();
        RegisteredServer target = event.getResult().getServer().orElse(null);

        if (target == null) {
            return;
        }

        ServerOnDemand server = OnDemandServerVelocity.getServerController().getServer(target.getServerInfo().getName());

        // If server is already running, allow connection
        if (OnDemandServerVelocity.getServerController().isServerStarted(server.getServerInfo())) {
            return;
        }

        StartingStatus startingStatus = server.start(p);

        if (startingStatus == StartingStatus.STARTING) {
            // Server startup has been initiated
            notifyPlayerServerStarting(p, target, server);
            event.setResult(ServerPreConnectEvent.ServerResult.denied());

            // Schedule countdown feedback
            scheduleCountdownTask(server);

        } else if (startingStatus == StartingStatus.ALREADY_STARTING) {
            // Server is already in the process of starting
            notifyPlayerServerAlreadyStarting(p, target, server);
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }

    /**
     * Notifies the player that the server is starting and either disconnects or sends a message.
     */
    private void notifyPlayerServerStarting(Player p, RegisteredServer target, ServerOnDemand server) {
        if (p.getCurrentServer().isEmpty()) {
            Component message = Component.text(MsgCFG.getContent(
                    "startup.proxy_join",
                    target.getServerInfo().getName(),
                    server.getRemainingStartTimeInSeconds()));
            p.disconnect(message);
        } else {
            Component message = Component.text(MsgCFG.getContent(
                    "startup.change_server",
                    target.getServerInfo().getName(),
                    server.getRemainingStartTimeInSeconds()));
            p.sendMessage(message);
        }
    }

    /**
     * Notifies the player that the server is already starting and either disconnects or sends a message.
     */
    private void notifyPlayerServerAlreadyStarting(Player p, RegisteredServer target, ServerOnDemand server) {
        if (p.getCurrentServer().isEmpty()) {
            Component message = Component.text(MsgCFG.getContent(
                    "already_starting.proxy_join",
                    target.getServerInfo().getName(),
                    server.getRemainingStartTimeInSeconds()));
            p.disconnect(message);
        } else {
            Component message = Component.text(MsgCFG.getContent(
                    "already_starting.change_server",
                    target.getServerInfo().getName(),
                    server.getRemainingStartTimeInSeconds()));
            p.sendMessage(message);
        }
    }

    /**
     * Schedules a countdown task to notify players about the server startup progress.
     *
     * @param server The server being started on-demand.
     */
    private void scheduleCountdownTask(ServerOnDemand server) {
        Scheduler scheduler = OnDemandServerVelocity.getProxyServer().getScheduler();
        CountdownTask countdownTask = new CountdownTask(server);

        ScheduledTask scheduledTask = scheduler.buildTask(OnDemandServerVelocity.getPlugin(), countdownTask)
                .repeat(1, TimeUnit.SECONDS)
                .schedule();

        countdownTask.setTask(scheduledTask);
    }
}
