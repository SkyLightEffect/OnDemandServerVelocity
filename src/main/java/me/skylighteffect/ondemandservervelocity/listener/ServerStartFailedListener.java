package me.skylighteffect.ondemandservervelocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import me.skylighteffect.ondemandservervelocity.configs.MsgCFG;
import me.skylighteffect.ondemandservervelocity.enums.ServerStatus;
import me.skylighteffect.ondemandservervelocity.events.ServerStartFailedEvent;
import net.kyori.adventure.text.Component;

import java.util.List;

public class ServerStartFailedListener {

    @Subscribe
    public void onServerStartFailed(ServerStartFailedEvent e) {
        long time = e.getPing().getTimeTook();

        e.getServer().setStatus(ServerStatus.STOPPED);

        List<Player> requester =  e.getServer().getRequester();

        for (Player p : requester) {
            Component message = Component.text(MsgCFG.getContent("start_failed", e.getServer().getServerInfo().getName(), time));
            p.sendMessage(message);
        }
    }
}
