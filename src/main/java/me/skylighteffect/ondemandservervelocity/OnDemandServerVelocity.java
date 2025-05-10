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

/**
 * Main class of the OnDemandServerVelocity plugin.
 * <p>
 * This plugin starts backend Minecraft servers on demand when a player attempts to connect.
 */
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

    /**
     * Constructor called by Velocity to initialize the plugin.
     *
     * @param server        the Velocity proxy server instance
     * @param logger        the logger for this plugin
     * @param dataDirectory the base data directory for the plugin's config files
     * @param plugin        the plugin container for metadata access
     */
    @Inject
    public OnDemandServerVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, PluginContainer plugin) {

        OnDemandServerVelocity.server = server;
        OnDemandServerVelocity.logger = logger;
        OnDemandServerVelocity.dataDirectory = dataDirectory;
        OnDemandServerVelocity.plugin = plugin;

        // Define plugin's working data folder
        OnDemandServerVelocity.dataFolder = new File(dataDirectory.toFile().getParent() + File.separator + "OnDemandServerVelocity");

        // Ensure the data directory exists
        if (!dataFolder.exists()) dataFolder.mkdirs();

        // Initialize configuration files
        MsgCFG.init(logger);
        MainCFG.init(logger);
        StatsCFG.init(logger);

        serverController = new ServerController(server);

        logger.info(MsgCFG.getContent("plugin_enabled", plugin.getDescription().getVersion()));
    }

    /**
     * Called when the proxy server has fully initialized.
     * Registers all relevant event listeners required by the plugin.
     *
     * @param e the initialization event
     */
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent e) {
        server.getEventManager().register(plugin, new ServerConnectListener());
        server.getEventManager().register(plugin, new ServerStartedListener());
        server.getEventManager().register(plugin, new ServerStartFailedListener());
    }

    /**
     * @return the Velocity logger instance used by the plugin
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * @return the ProxyServer instance
     */
    public static ProxyServer getProxyServer() {
        return server;
    }

    /**
     * @return the PluginContainer containing metadata about the plugin
     */
    public static PluginContainer getPlugin() {
        return plugin;
    }

    /**
     * @return the controller responsible for managing backend server processes
     */
    public static ServerController getServerController() {
        return serverController;
    }

    /**
     * @return the data folder where plugin files are stored
     */
    public static File getDataFolder() {
        return dataFolder;
    }
}