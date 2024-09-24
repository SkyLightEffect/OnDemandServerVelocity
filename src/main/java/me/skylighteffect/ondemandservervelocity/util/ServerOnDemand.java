package me.skylighteffect.ondemandservervelocity.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import me.skylighteffect.ondemandservervelocity.configs.MainCFG;
import me.skylighteffect.ondemandservervelocity.configs.StatsCFG;
import me.skylighteffect.ondemandservervelocity.enums.ServerStatus;
import me.skylighteffect.ondemandservervelocity.enums.StartingStatus;

import java.util.ArrayList;
import java.util.List;

public class ServerOnDemand {
    private final ServerInfo serverInfo;
    private ServerStatus status;
    private Process process;
    private final List<Player> requesters;
    private long startTime;

    public ServerOnDemand(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        this.status = ServerStatus.UNKNOWN;
        this.requesters = new ArrayList<>();
    }

    public StartingStatus start(Player player) {
        if (!requesters.contains(player)) {
            requesters.add(player);
        }

        return initiateStartProcess();
    }

    private StartingStatus initiateStartProcess() {
        switch (getStatus()) {
            case STARTED:
                return StartingStatus.ALREADY_STARTED;
            case STARTING:
                return StartingStatus.ALREADY_STARTING;
        }

        startServerProcess();
        return StartingStatus.STARTING;
    }

    private void startServerProcess() {
        OnDemandServerVelocity.getProxyServer().getScheduler().buildTask(OnDemandServerVelocity.getPlugin(), () -> new Ping(this)).schedule();

        ProcessBuilder pb = new ProcessBuilder(MainCFG.getScriptPath() + "/" + serverInfo.getName() + "/start.sh");
        OnDemandServerVelocity.getLogger().warn("Executing: {}/start.sh for server: {}", MainCFG.getScriptPath(), serverInfo.getName());

        try {
            status = ServerStatus.STARTING;
            process = pb.start();
            startTime = System.currentTimeMillis();
            OnDemandServerVelocity.getLogger().info("Server {} is starting...", serverInfo.getName());
        } catch (Exception e) {
            OnDemandServerVelocity.getLogger().error("Failed to start server: {}", serverInfo.getName(), e);
            status = ServerStatus.STOPPED; // Update status to stopped on failure
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

    public List<Player> getRequesters() {
        return requesters;
    }

    public void setStatus(ServerStatus status) {
        this.status = status;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void clearRequesters() {
        requesters.clear();
    }

    public int getRemainingStartTimeInSeconds() {
        long avgDuration = StatsCFG.getAvgStartDuration(serverInfo.getName());
        long elapsedTime = System.currentTimeMillis() - startTime;
        return (int) ((avgDuration - elapsedTime) / 1000);
    }

}
