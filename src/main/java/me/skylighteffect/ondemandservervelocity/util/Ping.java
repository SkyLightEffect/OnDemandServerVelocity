package me.skylighteffect.ondemandservervelocity.util;

import com.velocitypowered.api.proxy.ProxyServer;
import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import me.skylighteffect.ondemandservervelocity.configs.MainCFG;
import me.skylighteffect.ondemandservervelocity.events.ServerStartFailedEvent;
import me.skylighteffect.ondemandservervelocity.events.ServerStartedEvent;

import java.util.concurrent.TimeUnit;

public class Ping {
    private static final long MAX_STARTUP_TIME = MainCFG.getMaxStartupTimeMillis();
    private static final long PING_INTERVAL = 100;
    private final ServerOnDemand server;
    private final long timeout;
    private final long startTime;
    private long endTime;

    public Ping(ServerOnDemand server) {
        this(server, MAX_STARTUP_TIME);
    }

    public Ping(ServerOnDemand server, long timeout) {
        this.server = server;
        this.timeout = timeout;
        this.startTime = this.endTime = System.currentTimeMillis();
        ping();
    }

    private void ping() {
        if (hasTimedOut()) {
            OnDemandServerVelocity.getLogger().warn("Maximum ping attempts reached for {} server, aborting.", server.getServerInfo().getName());
            OnDemandServerVelocity.getProxyServer().getEventManager().fire(new ServerStartFailedEvent(server, this));
            return;
        }

        ProxyServer proxyServer = OnDemandServerVelocity.getProxyServer();

        proxyServer.getServer(server.getServerInfo().getName()).ifPresent(registeredServer -> {
            registeredServer.ping().whenComplete((serverPing, throwable) -> {
                endTime = System.currentTimeMillis();

                if (serverPing != null) {
                    OnDemandServerVelocity.getProxyServer().getEventManager().fire(new ServerStartedEvent(server, this));
                } else {
                    OnDemandServerVelocity.getLogger().debug("Ping failed for server {}. Retrying...", server.getServerInfo().getName());
                    scheduleNextPing();
                }
            });
        });
    }

    private boolean hasTimedOut() {
        return (System.currentTimeMillis() - startTime) > timeout;
    }

    private void scheduleNextPing() {
        OnDemandServerVelocity.getProxyServer().getScheduler().buildTask(OnDemandServerVelocity.getPlugin(), this::ping)
                .delay(PING_INTERVAL, TimeUnit.MILLISECONDS).schedule();
    }

    public long getTimeTook() {
        return this.endTime - this.startTime;
    }
}
