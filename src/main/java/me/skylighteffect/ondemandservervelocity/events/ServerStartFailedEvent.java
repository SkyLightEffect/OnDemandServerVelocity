package me.skylighteffect.ondemandservervelocity.events;

import me.skylighteffect.ondemandservervelocity.util.Ping;
import me.skylighteffect.ondemandservervelocity.util.ServerOnDemand;

public class ServerStartFailedEvent {
    private final ServerOnDemand server;

    private final Ping ping;

    public ServerStartFailedEvent(ServerOnDemand server, Ping ping) {
        this.ping = ping;
        this.server = server;
    }

    public ServerOnDemand getServer() {
        return this.server;
    }

    public Ping getPing() {
        return ping;
    }

}
