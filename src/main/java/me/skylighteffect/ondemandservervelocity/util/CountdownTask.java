package me.skylighteffect.ondemandservervelocity.util;

import com.velocitypowered.api.proxy.Player;
import me.skylighteffect.ondemandservervelocity.configs.MsgCFG;
import me.skylighteffect.ondemandservervelocity.configs.StatsCFG;
import net.kyori.adventure.text.Component;
import com.velocitypowered.api.scheduler.ScheduledTask;

import java.util.List;

public class CountdownTask implements Runnable {
    private final int NOTIFICATION_INTERVAL_LONG;
    private final int NOTIFICATION_INTERVAL_MEDIUM;
    private final int NOTIFICATION_INTERVAL_SHORT;

    private final ServerOnDemand server;
    private ScheduledTask task; // Store the scheduled task reference

    public CountdownTask(ServerOnDemand server) {
        this.server = server;
        this.task = null; // Placeholder for the task reference

        int avgTime = server.getRemainingStartTimeInSeconds();
        this.NOTIFICATION_INTERVAL_LONG = (int) (avgTime * 0.7);
        this.NOTIFICATION_INTERVAL_MEDIUM = (int) (avgTime * 0.3);
        this.NOTIFICATION_INTERVAL_SHORT = (int) (avgTime * 0.1);
    }

    public void setTask(ScheduledTask task) {
        this.task = task; // Set the task reference
    }

    @Override
    public void run() {
        int remainingTime = server.getRemainingStartTimeInSeconds();

        if (remainingTime <= 0) {
            // Stop the task if time is up
            for (Player p : server.getRequesters()) {
                p.sendMessage(Component.text("Der Server sollte nun verfügbar sein.")); // Notify players
            }
            if (task != null) {
                task.cancel(); // Cancel the scheduled task if it exists
            }
            return;
        }

        // Send countdown messages based on remaining time
        if (remainingTime == NOTIFICATION_INTERVAL_LONG) {
            String msg = MsgCFG.getRandomLongWaitMessage(MsgCFG.CountdownWait.LONG, server.getServerInfo().getName());
            notifyPlayers(msg);
        } else if (remainingTime == NOTIFICATION_INTERVAL_MEDIUM) {
            String msg = MsgCFG.getRandomLongWaitMessage(MsgCFG.CountdownWait.MEDIUM, server.getServerInfo().getName());
            notifyPlayers(msg);
        } else if (remainingTime == NOTIFICATION_INTERVAL_SHORT) {
            // For remaining time less than or equal to 10 seconds, send short messages
            String msg = MsgCFG.getRandomLongWaitMessage(MsgCFG.CountdownWait.SHORT, server.getServerInfo().getName());
            notifyPlayers(msg);
        }
    }

    private void notifyPlayers(String message) {
        if (message != null && !message.isEmpty()) {
            for (Player p : server.getRequesters()) {
                p.sendMessage(Component.text(message));
            }
        }
    }

}
