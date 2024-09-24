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

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServerConnectListener {

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player p = event.getPlayer();
        RegisteredServer target = event.getResult().getServer().orElse(null);
        if (target == null) {
            return;
        }

        ServerOnDemand server = OnDemandServerVelocity.getServerController().getServer(target.getServerInfo().getName());
        if (OnDemandServerVelocity.getServerController().isServerStarted(server.getServerInfo())) {
            return;
        }

        StartingStatus startingStatus = server.start(p);

        if (startingStatus == StartingStatus.STARTING) {
            if (p.getCurrentServer().isEmpty()) {
                Component message = Component.text(MsgCFG.getContent("startup.proxy_join", target.getServerInfo().getName(), server.getRemainingStartTimeInSeconds()));
                p.disconnect(message);
            } else {
                Component message = Component.text(MsgCFG.getContent("startup.change_server", target.getServerInfo().getName(), server.getRemainingStartTimeInSeconds()));
                p.sendMessage(message);
            }
            event.setResult(ServerPreConnectEvent.ServerResult.denied());


            // Start the countdown task
            Scheduler scheduler = OnDemandServerVelocity.getProxyServer().getScheduler();
            List<Player> players = server.getRequesters(); // Get all players requesting the server

            CountdownTask countdownTask = new CountdownTask(server);
            ScheduledTask scheduledTask = scheduler.buildTask(OnDemandServerVelocity.getPlugin(), countdownTask)
                    .repeat(1, TimeUnit.SECONDS) // run every second
                    .schedule(); // Schedule the task

            countdownTask.setTask(scheduledTask); // Set the scheduled task reference in the countdown task


        } else if (startingStatus == StartingStatus.ALREADY_STARTING) {
            if (p.getCurrentServer().isEmpty()) {
                Component message = Component.text(MsgCFG.getContent("already_starting.proxy_join", target.getServerInfo().getName(), server.getRemainingStartTimeInSeconds()));
                p.disconnect(message);
            } else {
                Component message = Component.text(MsgCFG.getContent("already_starting.change_server", target.getServerInfo().getName(), server.getRemainingStartTimeInSeconds()));
                p.sendMessage(message);
            }
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }
}
