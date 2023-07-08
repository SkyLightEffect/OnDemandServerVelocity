package me.skylighteffect.ondemandservervelocity.util;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import me.skylighteffect.ondemandservervelocity.enums.ServerStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;

public class ServerController {
    private final HashMap<ServerInfo, ServerOnDemand> servers;

    public ServerController(ProxyServer proxyServer) {
        this.servers = new HashMap<>();

        for (RegisteredServer registeredServer : proxyServer.getAllServers()) {
            ServerOnDemand server = new ServerOnDemand(registeredServer.getServerInfo());
            this.servers.put(registeredServer.getServerInfo(), server);

            if (isServerStarted(registeredServer.getServerInfo())) {
                server.setStatus(ServerStatus.STARTED);
            } else {
                server.setStatus(ServerStatus.STOPPED);
            }
        }
    }

    public ServerOnDemand getServer(ServerInfo serverInfo) {
        if (!this.servers.containsKey(serverInfo)) return null;
        return this.servers.get(serverInfo);
    }

    public boolean isServerStarted(ServerInfo serverInfo) {
        // boolean serverRunningOnPort = registeredServer.getPlayersConnected().size() > 0;

        int port = ((InetSocketAddress) serverInfo.getAddress()).getPort();
        boolean serverRunningOnPort = !isAvailable(port);

        boolean processRunning = this.getServer(serverInfo).getProcess() != null && this.getServer(serverInfo).getProcess().isAlive();

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
