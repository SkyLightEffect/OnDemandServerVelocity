package me.skylighteffect.ondemandservervelocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import me.skylighteffect.ondemandservervelocity.configs.MsgCFG;
import me.skylighteffect.ondemandservervelocity.configs.StatsCFG;
import me.skylighteffect.ondemandservervelocity.enums.ServerStatus;
import me.skylighteffect.ondemandservervelocity.events.ServerStartedEvent;
import net.kyori.adventure.text.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Listener that reacts when a server finishes starting up.
 * <p>
 * This class handles player notification, records server start time,
 * and automatically redirects requesters to the now-available server.
 */
public class ServerStartedListener {

    /**
     * Called when a server has successfully started.
     * <p>
     * Notifies all requesting players, redirects them to the server if applicable,
     * logs the startup time, and stores statistics for future reference.
     *
     * @param e the event containing information about the started server.
     */
    @Subscribe
    public void onServerStarted(ServerStartedEvent e) {
        long time = e.getPing().getTimeTook();

        // Log successful startup
        OnDemandServerVelocity.getLogger().info(MsgCFG.getContent("start_successful",
                e.getServer().getServerInfo().getName(), time));

        // Update server status
        e.getServer().setStatus(ServerStatus.STARTED);

        // Create a snapshot of requesters to avoid concurrent modification
        Set<Player> requesters = new HashSet<>(e.getServer().getRequesters());

        // Notify and redirect players
        for (Player p : requesters) {
            if (p != null && p.getCurrentServer().isPresent()) {
                Component message = Component.text(MsgCFG.getContent("start_successful",
                        e.getServer().getServerInfo().getName(), time));
                p.sendMessage(message);

                RegisteredServer targetServer = OnDemandServerVelocity.getProxyServer()
                        .getServer(e.getServer().getServerInfo().getName())
                        .orElse(null);

                if (targetServer != null) {
                    p.createConnectionRequest(targetServer).fireAndForget();
                }
            }
        }

        // Store the startup time for statistics
        StatsCFG.saveStartDuration(e.getServer().getServerInfo().getName(), time);

        // Clear requesters list
        e.getServer().clearRequesters();
    }
}
