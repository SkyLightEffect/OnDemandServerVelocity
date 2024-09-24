package me.skylighteffect.ondemandservervelocity.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import me.skylighteffect.ondemandservervelocity.configs.MainCFG;
import me.skylighteffect.ondemandservervelocity.enums.ServerStatus;
import me.skylighteffect.ondemandservervelocity.enums.StartingStatus;

import java.util.ArrayList;
import java.util.List;

public class ServerOnDemand {
    private final ServerInfo serverInfo;
    // private final RegisteredServer server;
    private ServerStatus status;
    private Process process;
    private final List<Player> requester;

    public ServerOnDemand(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        this.status = ServerStatus.UNKNOWN;
        requester = new ArrayList<>();
    }

    public StartingStatus start(Player p) {
        StartingStatus status = start();

        /*
        if (status == StartingStatus.STARTING) {
            this.requester = p;
        }
        */
        if (!requester.contains(p))
            requester.add(p);

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
        OnDemandServerVelocity.getLogger().warn("Execute {}/{}/start.sh", MainCFG.getScriptPath(), serverInfo.getName());

        try {
            status = ServerStatus.STARTING;
            process = pb.start();
            return StartingStatus.STARTING;
        } catch (Exception e) {
            e.printStackTrace();
            return StartingStatus.UNKNOWN;
        }
    }

    public ServerStatus getStatus() {
        if (status == ServerStatus.STARTING) return status;
        if (!OnDemandServerVelocity.getServerController().isServerStarted(serverInfo)) {
            status = ServerStatus.STOPPED;
        }
        return status;
    }

    public Process getProcess() {
        return process;
    }

    public List<Player> getRequester() {
        return requester;
    }

    public void setStatus(ServerStatus status) {
        this.status = status;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void clearRequesters() {
        requester.clear();
    }
}
