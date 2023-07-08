package me.skylighteffect.ondemandservervelocity.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import me.skylighteffect.ondemandservervelocity.configs.MainCFG;
import me.skylighteffect.ondemandservervelocity.enums.ServerStatus;
import me.skylighteffect.ondemandservervelocity.enums.StartingStatus;
import net.kyori.adventure.identity.Identity;

public class ServerOnDemand {
    private final ServerInfo serverInfo;
    // private final RegisteredServer server;
    private ServerStatus status;
    private Process process;
    private Player requester;

    public ServerOnDemand(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        this.status = ServerStatus.UNKNOWN;
    }

    public StartingStatus start(Player p) {
        StartingStatus status = start();

        if (status == StartingStatus.STARTING) {
            this.requester = p;
        }
        return status;
    }

    private StartingStatus start() {
        switch (getStatus()) {
            case STARTED:
                return StartingStatus.ALREADY_STARTED;
            case STARTING:
                return StartingStatus.ALREADY_STARTING;
        }

        // Ping server until it is started
        OnDemandServerVelocity.getProxyServer().getScheduler().buildTask(OnDemandServerVelocity.getPlugin(), () -> new Ping(this)).schedule();

        ProcessBuilder pb = new ProcessBuilder(MainCFG.getScriptPath() + "/" + serverInfo.getName() + "/start.sh");
        try {
            process = pb.start();
            status = ServerStatus.STARTING;
            return StartingStatus.STARTING;
        } catch (Exception e) {
            e.printStackTrace();
            return StartingStatus.UNKNOWN;
        }
    }

    public ServerStatus getStatus() {
        if (OnDemandServerVelocity.getProxyServer().getServer(serverInfo.getName()).isEmpty()) {
            status = ServerStatus.STOPPED;
        }
        return status;
    }

    public Process getProcess() {
        return process;
    }

    public Player getRequester() {
        return requester;
    }

    public void setStatus(ServerStatus status) {
        this.status = status;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }
}