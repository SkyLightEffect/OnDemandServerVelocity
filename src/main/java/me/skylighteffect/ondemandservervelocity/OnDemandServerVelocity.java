package me.skylighteffect.ondemandservervelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.PluginContainer; // Importiere PluginContainer
import me.skylighteffect.ondemandservervelocity.configs.MainCFG;
import me.skylighteffect.ondemandservervelocity.configs.MsgCFG;
import me.skylighteffect.ondemandservervelocity.configs.StatsCFG;
import me.skylighteffect.ondemandservervelocity.listener.ServerConnectListener;
import me.skylighteffect.ondemandservervelocity.listener.ServerStartFailedListener;
import me.skylighteffect.ondemandservervelocity.listener.ServerStartedListener;
import me.skylighteffect.ondemandservervelocity.util.ServerController;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

@Plugin(
        id = "ondemandservervelocity",
        name = "OnDemandServerVelocity",
        version = "1.10.3-SNAPSHOT",
        authors = {"SkyLightEffect"}
)
public class OnDemandServerVelocity {

    private static ProxyServer server;
    private static Logger logger;
    private static Path dataDirectory;
    private static PluginContainer plugin;

    private static ServerController serverController;

    private static File dataFolder;

    @Inject
    public OnDemandServerVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, PluginContainer plugin) {

        OnDemandServerVelocity.server = server;
        OnDemandServerVelocity.logger = logger;
        OnDemandServerVelocity.dataDirectory = dataDirectory;
        OnDemandServerVelocity.plugin = plugin;


        OnDemandServerVelocity.dataFolder = new File(dataDirectory.toFile().getParent() + File.separator + "OnDemandServerVelocity");

        // Write default configuration folder
        if (!dataFolder.mkdirs())
            dataFolder.mkdir();

        MsgCFG.init(logger);
        MainCFG.init(logger);
        StatsCFG.init(logger);

        serverController = new ServerController(server);

        logger.info(MsgCFG.getContent("plugin_enabled", plugin.getDescription().getVersion()));
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent e) {
        server.getEventManager().register(plugin, new ServerConnectListener());
        server.getEventManager().register(plugin, new ServerStartedListener());
        server.getEventManager().register(plugin, new ServerStartFailedListener());
    }

    public static Logger getLogger() {
        return logger;
    }

    public static ProxyServer getProxyServer() {
        return server;
    }

    public static PluginContainer getPlugin() {
        return plugin;
    }

    public static ServerController getServerController() {
        return serverController;
    }

    public static File getDataFolder() {
        return dataFolder;
    }
}