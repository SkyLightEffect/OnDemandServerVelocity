package me.skylighteffect.ondemandservervelocity.util;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import me.skylighteffect.ondemandservervelocity.enums.ServerStatus;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

/**
 * Controls and monitors the lifecycle of on-demand Minecraft server instances registered with the Velocity proxy.
 * <p>
 * <ul>
 *     <li>Discovers all currently registered servers on initialization.</li>
 *     <li>Maintains a mapping from server name to {@link ServerOnDemand} instance.</li>
 *     <li>Provides methods to query server status (running or stopped) and to inspect port availability.</li>
 *     <li>Logs initial status on startup and can print status at any time.</li>
 * </ul>
 * </p>
 * <p>
 * Usage in production ensures that backend servers are started automatically when players connect,
 * and prevents port conflicts by checking socket availability.
 * </p>
 *
 * @author SkyLightEffect
 */
public class ServerController {

    /**
     * Map of registered server names to their on-demand controller instances.
     */
    private final HashMap<String, ServerOnDemand> servers;

    /**
     * Initializes the controller by scanning all servers currently registered with Velocity.
     * Sets the initial {@link ServerStatus} based on port checks or process liveness.
     * <p>
     * Each discovered server is wrapped in a {@link ServerOnDemand} and added to the internal map.
     * </p>
     *
     * @param proxyServer the Velocity proxy server instance
     */
    public ServerController(ProxyServer proxyServer) {
        this.servers = new HashMap<>();

        for (RegisteredServer registeredServer : proxyServer.getAllServers()) {
            ServerInfo info = registeredServer.getServerInfo();
            ServerOnDemand sod = new ServerOnDemand(info);
            this.servers.put(info.getName(), sod);

            // Determine and log initial status
            if (isServerStarted(info)) {
                sod.setStatus(ServerStatus.STARTED);
                System.out.println(info.getName() + " (STARTED)");
            } else {
                sod.setStatus(ServerStatus.STOPPED);
                System.out.println(info.getName() + " (STOPPED)");
            }
        }
    }

    /**
     * Retrieves the on-demand controller for a given server by name.
     *
     * @param serverName unique identifier of the registered server
     * @return the {@link ServerOnDemand} instance, or {@code null} if not found
     */
    public ServerOnDemand getServer(String serverName) {
        return this.servers.get(serverName);
    }

    /**
     * Checks whether the specified server is currently running.
     * <p>
     * A server is considered started if either:
     * <ul>
     *     <li>An underlying process is alive, or</li>
     *     <li>The configured port is in use by another application.</li>
     * </ul>
     * </p>
     *
     * @param serverInfo metadata about the target server
     * @return {@code true} if the server process is alive or the port is occupied; {@code false} otherwise
     */
    public boolean isServerStarted(ServerInfo serverInfo) {
        int port = serverInfo.getAddress().getPort();
        boolean portTaken = !isAvailable(port);
        boolean processAlive = false;

        ServerOnDemand sod = getServer(serverInfo.getName());
        if (sod != null && sod.getProcess() != null) {
            processAlive = sod.getProcess().isAlive();
        }

        return portTaken || processAlive;
    }

    /**
     * Determines whether a TCP port is available on the host machine.
     * <p>
     * Attempts to bind a temporary {@link ServerSocket} to the target port:
     * if binding succeeds, the port is free; otherwise, it is occupied.
     * </p>
     *
     * @param portNr the port number to test
     * @return {@code true} if the port is free; {@code false} if it is in use
     */
    public static boolean isAvailable(int portNr) {
        try (ServerSocket socket = new ServerSocket(portNr)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Prints the current status of all managed servers to standard output.
     * <p>
     * Format: "&lt;serverName&gt; (&lt;status&gt;)"
     * </p>
     */
    public void printStatus() {
        for (ServerOnDemand sod : servers.values()) {
            System.out.println(sod.getServerInfo().getName() + " (" + sod.getStatus() + ")");
        }
    }
}
