package me.skylighteffect.ondemandservervelocity.configs;

import com.velocitypowered.api.proxy.ProxyServer;
import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StatsCFG {
    private static ConfigurationNode config;
    private static final String FILE_NAME = "stats.yml";

    public static void init(Logger logger) {
        config = ConfigLoader.loadConfig(FILE_NAME, logger);
    }

    // Method to save the start duration of a server
    public static void saveStartDuration(String serverName, long duration) {
        try {
            ConfigurationNode serverNode = config.node(serverName);
            List<Long> durations = serverNode.node("start_durations").getList(Long.class);

            if (durations == null) {
                durations = new ArrayList<>();
            }

            // Add new duration and maintain only the last 3
            durations.add(duration);
            if (durations.size() > 3) {
                durations.remove(0);
            }

            // Save back to config
            serverNode.node("start_durations").set(durations);

            // Save the config to file
            YamlConfigurationLoader.builder()
                    .path(OnDemandServerVelocity.getDataFolder().toPath().resolve(FILE_NAME))
                    .build()
                    .save(config);

        } catch (Exception e) {
            e.printStackTrace();
            // Log an error if saving or accessing the durations fails
            OnDemandServerVelocity.getLogger().error("Failed to save start duration for server {}: {}", serverName, e.getMessage());
        }
    }

    // Method to retrieve the last 3 start durations for a server
    public static List<Long> getLastStartDurations(String serverName) {
        List<Long> durations = new ArrayList<>();
        try {
            ConfigurationNode serverNode = config.node(serverName);
            durations = serverNode.node("start_durations").getList(Long.class);

            if (durations == null) {
                durations = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log an error if retrieving durations fails
            OnDemandServerVelocity.getLogger().error("Failed to retrieve start durations for server {}: {}", serverName, e.getMessage());
        }
        return durations;
    }

    public static long getAvgStartDuration(String serverName) {
        long avgStartDuration = 0;

        List<Long> durations = getLastStartDurations(serverName);
        if (durations.isEmpty()) return 0;

        for (Long duration : durations) {
            avgStartDuration += duration;
        }
        return avgStartDuration / durations.size();
    }
}
