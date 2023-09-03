package me.skylighteffect.ondemandservervelocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import me.skylighteffect.ondemandservervelocity.configs.MsgCFG;
import me.skylighteffect.ondemandservervelocity.enums.StartingStatus;
import me.skylighteffect.ondemandservervelocity.util.ServerOnDemand;
import net.kyori.adventure.text.Component;

public class ServerConnectListener {

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player p = event.getPlayer();
        RegisteredServer target = event.getResult().getServer().orElse(null);
        if (target == null) {
            return;
        }

        ServerOnDemand server = OnDemandServerVelocity.getServerController().getServer(target.getServerInfo().getName());
        if (OnDemandServerVelocity.getServerController().isServerStarted(server.getServerInfo())) {
            return;
        }

        StartingStatus startingStatus = server.start(p);

        if (startingStatus == StartingStatus.STARTING) {
            if (p.getCurrentServer().isEmpty()) {
                Component message = Component.text(MsgCFG.getContent("startup.proxy_join", target.getServerInfo().getName()));
                p.disconnect(message);
            } else {
                Component message = Component.text(MsgCFG.getContent("startup.change_server", target.getServerInfo().getName()));
                p.sendMessage(message);
            }
            event.setResult(ServerPreConnectEvent.ServerResult.denied());

        } else if (startingStatus == StartingStatus.ALREADY_STARTING) {
            if (p.getCurrentServer().isEmpty()) {
                Component message = Component.text(MsgCFG.getContent("already_starting.proxy_join", target.getServerInfo().getName()));
                p.disconnect(message);
            } else {
                Component message = Component.text(MsgCFG.getContent("already_starting.change_server", target.getServerInfo().getName()));
                p.sendMessage(message);
            }
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }
}
