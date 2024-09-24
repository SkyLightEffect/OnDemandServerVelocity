package me.skylighteffect.ondemandservervelocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import me.skylighteffect.ondemandservervelocity.configs.MsgCFG;
import me.skylighteffect.ondemandservervelocity.enums.ServerStatus;
import me.skylighteffect.ondemandservervelocity.events.ServerStartFailedEvent;
import net.kyori.adventure.text.Component;

import java.util.HashSet;
import java.util.Set;

public class ServerStartFailedListener {

    @Subscribe
    public void onServerStartFailed(ServerStartFailedEvent e) {
        long time = e.getPing().getTimeTook();

        e.getServer().setStatus(ServerStatus.STOPPED);

        Set<Player> requesters = new HashSet<>(e.getServer().getRequesters());

        for (Player p : requesters) {
            Component message = Component.text(MsgCFG.getContent("start_failed", e.getServer().getServerInfo().getName(), time));
            p.sendMessage(message);
        }

        e.getServer().clearRequesters();
    }
}
