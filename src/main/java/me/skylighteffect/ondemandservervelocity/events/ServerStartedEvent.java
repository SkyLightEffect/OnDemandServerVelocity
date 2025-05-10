package me.skylighteffect.ondemandservervelocity.events;

import me.skylighteffect.ondemandservervelocity.util.Ping;
import me.skylighteffect.ondemandservervelocity.util.ServerOnDemand;

/**
 * Event fired when a server has successfully started.
 */
public class ServerStartedEvent {

    private final ServerOnDemand server;
    private final Ping ping;

    /**
     * Constructs a new ServerStartedEvent.
     *
     * @param server The {@link ServerOnDemand} instance that started.
     * @param ping   The {@link Ping} result measuring the start time.
     * @throws IllegalArgumentException if server or ping is null.
     */
    public ServerStartedEvent(ServerOnDemand server, Ping ping) {
        if (server == null || ping == null) {
            throw new IllegalArgumentException("Server and ping must not be null");
        }
        this.server = server;
        this.ping = ping;
    }

    /**
     * @return the started {@link ServerOnDemand} instance.
     */
    public ServerOnDemand getServer() {
        return server;
    }

    /**
     * @return the {@link Ping} measurement of the startup time.
     */
    public Ping getPing() {
        return ping;
    }
}
