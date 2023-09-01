package me.skylighteffect.ondemandservervelocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import me.skylighteffect.ondemandservervelocity.configs.MsgCFG;
import me.skylighteffect.ondemandservervelocity.enums.ServerStatus;
import me.skylighteffect.ondemandservervelocity.events.ServerStartedEvent;
import net.kyori.adventure.text.Component;

import java.util.List;

public class ServerStartedListener {
    @Subscribe
    public void onServerStarted(ServerStartedEvent e) {
        long time = e.getPing().getTimeTook();

        OnDemandServerVelocity.getLogger().info(MsgCFG.getContent("start_successful", e.getServer().getServerInfo().getName(), time));

        e.getServer().setStatus(ServerStatus.STARTED);

        List<Player> requester = e.getServer().getRequester();

        for (Player p : requester) {
            if (p != null && p.getCurrentServer().isPresent()) {
                Component message = Component.text(MsgCFG.getContent("start_successful", e.getServer().getServerInfo().getName(), time));
                p.sendMessage(message);

                RegisteredServer server = OnDemandServerVelocity.getProxyServer().getServer(e.getServer().getServerInfo().getName()).orElse(null);

                p.createConnectionRequest(server).fireAndForget();
            }
        }

        e.getServer().clearRequesters();
    }
}
