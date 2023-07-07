package me.skylighteffect.ondemandservervelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.PluginContainer; // Importiere PluginContainer
import me.skylighteffect.ondemandservervelocity.configs.MainCFG;
import me.skylighteffect.ondemandservervelocity.configs.MsgCFG;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "ondemandservervelocity",
        name = "OnDemandServerVelocity",
        version = "1.2-SNAPSHOT"
)
public class OnDemandServerVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final PluginContainer pluginContainer;

    @Inject
    public OnDemandServerVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, PluginContainer pluginContainer) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.pluginContainer = pluginContainer;
        MsgCFG.loadConfig(dataDirectory, server, logger);
        MainCFG.loadConfig(dataDirectory, server, logger);

        logger.info(MsgCFG.getContent("plugin_enabled", pluginContainer.getDescription().getVersion()));
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

    }
}