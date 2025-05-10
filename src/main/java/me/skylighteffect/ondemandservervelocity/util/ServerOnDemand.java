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

/**
 * Represents a Minecraft server configured in Velocity that can be started on-demand.
 * <p>
 * Tracks the server's current {@link ServerStatus}, startup {@link Process}, requesting players,
 * and timing information for analytics and user feedback.
 * </p>
 * <p>
 * Typical lifecycle:
 * <ol>
 *     <li>User attempts to connect to a server.</li>
 *     <li>{@link #start(Player)} registers the requester and initiates startup if not already running.</li>
 *     <li>Startup is performed asynchronously; status transitions:
 *         {@link ServerStatus#STARTING} → {@link ServerStatus#STARTED} or {@link ServerStatus#STOPPED}.</li>
 *     <li>Players receive countdown updates via {@link me.skylighteffect.ondemandservervelocity.util.CountdownTask}
 *         and are redirected once the server is online.</li>
 *     <li>Startup durations are recorded via {@link StatsCFG}.</li>
 * </ol>
 * </p>
 *
 * @author SkyLightEffect
 */
public class ServerOnDemand {
    /** Immutable metadata about the server (hostname, port, name). */
    private final ServerInfo serverInfo;

    /** Current status of the server. */
    private ServerStatus status;

    /** The process instance if the server has been launched; null otherwise. */
    private Process process;

    /** Players who have requested the server start; used for notifications and redirection. */
    private final List<Player> requesters;

    /** Timestamp in milliseconds when the start process was initiated. */
    private long startTime;

    /**
     * Creates a new on-demand controller for a given server.
     *
     * @param serverInfo the Velocity-registered server info
     */
    public ServerOnDemand(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        this.status = ServerStatus.UNKNOWN;
        this.requesters = new ArrayList<>();
    }

    /**
     * Registers a player as a requester and attempts to start the server.
     *
     * @param player the player requesting the server
     * @return a {@link StartingStatus} indicating whether the start was initiated, already started, etc.
     */
    public StartingStatus start(Player player) {
        if (!requesters.contains(player)) {
            requesters.add(player);
        }
        return initiateStartProcess();
    }

    /**
     * Determines the next action based on current status and initiates startup if needed.
     *
     * @return the resulting {@link StartingStatus}
     */
    private StartingStatus initiateStartProcess() {
        switch (getStatus()) {
            case STARTED:
                return StartingStatus.ALREADY_STARTED;
            case STARTING:
                return StartingStatus.ALREADY_STARTING;
            default:
                // FALLTHROUGH: proceed to start the server
        }

        startServerProcess();
        return StartingStatus.STARTING;
    }

    /**
     * Launches the server start script and schedules a ping task for completion detection.
     * Updates status, spawns the process, and logs appropriately.
     */
    private void startServerProcess() {
        // Schedule a task to ping the server and fire events
        OnDemandServerVelocity.getProxyServer()
                .getScheduler()
                .buildTask(OnDemandServerVelocity.getPlugin(), () -> new Ping(this))
                .schedule();

        String scriptPath = MainCFG.getScriptPath() + "/" + serverInfo.getName() + "/start.sh";
        OnDemandServerVelocity.getLogger().warn("Executing start script for server {}: {}",
                serverInfo.getName(), scriptPath);

        try {
            status = ServerStatus.STARTING;
            process = new ProcessBuilder(scriptPath).start();
            startTime = System.currentTimeMillis();
            OnDemandServerVelocity.getLogger().info("Server {} is starting...", serverInfo.getName());
        } catch (Exception e) {
            OnDemandServerVelocity.getLogger().error(
                    "Failed to start server {}:", serverInfo.getName(), e);
            status = ServerStatus.STOPPED;
        }
    }

    /**
     * Returns the current status; refreshes to STOPPED if not actually running.
     *
     * @return the {@link ServerStatus}
     */
    public ServerStatus getStatus() {
        if (status == ServerStatus.STARTING) {
            return status;
        }
        if (!OnDemandServerVelocity.getServerController().isServerStarted(serverInfo)) {
            status = ServerStatus.STOPPED;
        }
        return status;
    }

    /** @return the underlying process, or null if not started. */
    public Process getProcess() {
        return process;
    }

    /** @return an unmodifiable view of the players awaiting server startup. */
    public List<Player> getRequesters() {
        return List.copyOf(requesters);
    }

    /**
     * Manually sets the server's status. Primarily used by event listeners.
     *
     * @param status new status
     */
    public void setStatus(ServerStatus status) {
        this.status = status;
    }

    /** @return the server's Velocity metadata. */
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    /** Clears the list of players who requested this server. */
    public void clearRequesters() {
        requesters.clear();
    }

    /**
     * Calculates the remaining estimated time (in seconds) until server startup completes,
     * based on historical averages from {@link StatsCFG}.
     *
     * @return remaining time in seconds; may be negative if overdue.
     */
    public int getRemainingStartTimeInSeconds() {
        long avgDuration = StatsCFG.getAvgStartDuration(serverInfo.getName());
        long elapsed = System.currentTimeMillis() - startTime;
        return (int) ((avgDuration - elapsed) / 1000);
    }
}
