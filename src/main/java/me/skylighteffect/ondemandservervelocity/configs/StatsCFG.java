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
    private static final String filename = "stats.yml";

    public static void loadConfig(Path path, ProxyServer proxy, Logger logger) {
        // Determine the path to the target folder ("OnDemandServerVelocity" subfolder)
        File targetFolder = OnDemandServerVelocity.getDataFolder();

        // Check if the target folder already exists
        if (!targetFolder.exists()) {
            // Create the target folder if it doesn't exist
            targetFolder.mkdirs();
        }

        // Determine the path to the target file inside the target folder
        File targetFile = new File(targetFolder, filename);

        // Check if the target file already exists
        if (!targetFile.exists()) {
            // Copy the stats.yml file from the .jar file to the target folder
            try (InputStream inputStream = OnDemandServerVelocity.class.getResourceAsStream("/" + filename)) {
                assert inputStream != null;
                Files.copy(inputStream, targetFile.toPath());
                System.out.println("The stats.yml file has been successfully copied.");
            } catch (IOException e) {
                logger.error("Failed to load configuration from {}", targetFile.getPath(), e);
            }

        } else {
            System.out.println("The stats.yml file already exists in the target folder.");
        }

        // Load config using the new Configurate API
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(targetFile.toPath()) // Use path method in the new API
                .build();

        try {
            config = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    .path(OnDemandServerVelocity.getDataFolder().toPath().resolve(filename))
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
