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
    private final HashMap<String, ServerOnDemand> servers;

    public ServerController(ProxyServer proxyServer) {
        this.servers = new HashMap<>();

        for (RegisteredServer registeredServer : proxyServer.getAllServers()) {

            ServerOnDemand server = new ServerOnDemand(registeredServer.getServerInfo());
            this.servers.put(registeredServer.getServerInfo().getName(), server);

            if (isServerStarted(registeredServer.getServerInfo())) {
                server.setStatus(ServerStatus.STARTED);
                System.out.println(registeredServer.getServerInfo().getName() + " (STARTED)");
            } else {
                server.setStatus(ServerStatus.STOPPED);
                System.out.println(registeredServer.getServerInfo().getName() + " (STOPPED)");
            }


        }
    }

    /*
    public ServerOnDemand getServer(ServerInfo serverInfo) {
        if (!this.servers.containsKey(serverInfo)) return null;
        return this.servers.get(serverInfo);
    }
     */

    public ServerOnDemand getServer(String serverName) {
        if (!this.servers.containsKey(serverName)) return null;
        return this.servers.get(serverName);
    }

    public boolean isServerStarted(ServerInfo serverInfo) {
        // boolean serverRunningOnPort = registeredServer.getPlayersConnected().size() > 0;

        int port = ((InetSocketAddress) serverInfo.getAddress()).getPort();
        boolean serverRunningOnPort = !isAvailable(port);

        boolean processRunning = this.getServer(serverInfo.getName()).getProcess() != null && this.getServer(serverInfo.getName()).getProcess().isAlive();

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

    public void printStatus() {
        for (ServerOnDemand s : servers.values()) {
            System.out.println(s.getServerInfo().getName() + " (" + s.getStatus() + ")");
        }
    }
}
