package me.skylighteffect.ondemandservervelocity.util;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.skylighteffect.ondemandservervelocity.enums.ServerStatus;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class ServerController {
    private final HashMap<RegisteredServer, ServerOnDemand> servers;

    public ServerController(ProxyServer proxyServer) {
        this.servers = new HashMap<>();

        for (RegisteredServer registeredServer : proxyServer.getAllServers()) {
            ServerOnDemand server = new ServerOnDemand(registeredServer.getServerInfo());
            this.servers.put(registeredServer, server);

            if (isServerStarted(registeredServer)) {
                server.setStatus(ServerStatus.STARTED);
            } else {
                server.setStatus(ServerStatus.STOPPED);
            }
        }
    }

    public ServerOnDemand getServer(RegisteredServer registeredServer) {
        if (!this.servers.containsKey(registeredServer)) return null;
        return this.servers.get(registeredServer);
    }

    public boolean isServerStarted(RegisteredServer registeredServer) {
        boolean serverRunningOnPort = registeredServer.getPlayersConnected().size() > 0;

        boolean processRunning = this.getServer(registeredServer).getProcess() != null && this.getServer(registeredServer).getProcess().isAlive();

        return serverRunningOnPort || processRunning;
    }

    public static boolean isAvailable(int portNr) {
        boolean portFree;
        try (ServerSocket ignored = new ServerSocket(portNr)) {
            portFree = true;
        } catch (IOException e) {
            portFree = false;
        }
        return portFree;
    }
}
