package me.skylighteffect.ondemandservervelocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import me.skylighteffect.ondemandservervelocity.configs.MsgCFG;
import me.skylighteffect.ondemandservervelocity.enums.ServerStatus;
import me.skylighteffect.ondemandservervelocity.events.ServerStartFailedEvent;
import net.kyori.adventure.text.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Listener for handling scenarios where a server fails to start.
 * <p>
 * Notifies all players who requested the server and resets the server status.
 */
public class ServerStartFailedListener {

    /**
     * Called when a server fails to start.
     *
     * @param e the event containing the failed server and the duration it attempted to start.
     */
    @Subscribe
    public void onServerStartFailed(ServerStartFailedEvent e) {
        long time = e.getPing().getTimeTook();

        // Mark the server as stopped
        e.getServer().setStatus(ServerStatus.STOPPED);

        // Get a copy of the requesters to avoid concurrent modification
        Set<Player> requesters = new HashSet<>(e.getServer().getRequesters());

        // Notify each requester of the failure
        for (Player p : requesters) {
            Component message = Component.text(MsgCFG.getContent("start_failed",
                    e.getServer().getServerInfo().getName(), time));
            p.sendMessage(message);
        }

        // Clear the requester list to reset the server's state
        e.getServer().clearRequesters();
    }
}
