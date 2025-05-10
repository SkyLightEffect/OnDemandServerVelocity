package me.skylighteffect.ondemandservervelocity.events;

import me.skylighteffect.ondemandservervelocity.util.Ping;
import me.skylighteffect.ondemandservervelocity.util.ServerOnDemand;

/**
 * Event fired when a server fails to start.
 */
public class ServerStartFailedEvent {

    private final ServerOnDemand server;
    private final Ping ping;

    /**
     * Constructs a new ServerStartFailedEvent.
     *
     * @param server The {@link ServerOnDemand} instance that failed to start.
     * @param ping   The {@link Ping} result measuring the attempted startup time.
     * @throws IllegalArgumentException if server or ping is null.
     */
    public ServerStartFailedEvent(ServerOnDemand server, Ping ping) {
        if (server == null || ping == null) {
            throw new IllegalArgumentException("Server and ping must not be null");
        }
        this.server = server;
        this.ping = ping;
    }

    /**
     * @return the failed {@link ServerOnDemand} instance.
     */
    public ServerOnDemand getServer() {
        return server;
    }

    /**
     * @return the {@link Ping} measurement of the attempted startup time.
     */
    public Ping getPing() {
        return ping;
    }
}
