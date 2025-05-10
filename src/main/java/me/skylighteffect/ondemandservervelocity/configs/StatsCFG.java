package me.skylighteffect.ondemandservervelocity.configs;

import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code StatsCFG} manages persistent storage and retrieval of server start-up statistics.
 * <p>
 * This class handles saving the duration of server start operations to a YAML configuration file
 * and computing statistics such as average start time based on the last few recorded values.
 * </p>
 * <p>
 * All data is stored under the plugin's data directory in {@code stats.yml}.
 * </p>
 *
 * @author SkyLightEffect
 */
public class StatsCFG {

    /**
     * The root configuration node for stats.yml.
     */
    private static ConfigurationNode config;

    /**
     * Name of the configuration file where stats are stored.
     */
    private static final String FILE_NAME = "stats.yml";

    /**
     * Initializes the stats configuration by loading or creating {@code stats.yml}.
     *
     * @param logger the SLF4J logger for reporting any load errors
     */
    public static void init(Logger logger) {
        config = ConfigLoader.loadConfig(FILE_NAME, logger);
    }

    /**
     * Records a new server start duration for the specified server.
     * <p>
     * Maintains only the last three recorded durations to cap storage size.
     * Saves the updated list back to disk.
     * </p>
     *
     * @param serverName unique identifier of the server
     * @param duration   time in milliseconds that the server took to start
     */
    public static void saveStartDuration(String serverName, long duration) {
        try {
            ConfigurationNode serverNode = config.node(serverName);
            List<Long> durations = serverNode.node("start_durations").getList(Long.class);

            if (durations == null) {
                durations = new ArrayList<>();
            }

            // Append new duration and enforce maximum of 3 entries
            durations.add(duration);
            if (durations.size() > 3) {
                durations.remove(0);
            }

            serverNode.node("start_durations").set(durations);

            // Persist changes to stats.yml
            YamlConfigurationLoader.builder()
                    .path(OnDemandServerVelocity.getDataFolder().toPath().resolve(FILE_NAME))
                    .build()
                    .save(config);
        } catch (Exception e) {
            // Log with context and avoid stack trace in production
            OnDemandServerVelocity.getLogger().error(
                    "[StatsCFG] Failed to save start duration for server {}: {}",
                    serverName, e.getMessage(), e
            );
        }
    }

    /**
     * Retrieves the last up to three recorded start durations for the given server.
     *
     * @param serverName unique identifier of the server
     * @return list of durations in milliseconds; empty list if none recorded or on error
     */
    public static List<Long> getLastStartDurations(String serverName) {
        List<Long> durations = new ArrayList<>();
        try {
            ConfigurationNode serverNode = config.node(serverName);
            List<Long> stored = serverNode.node("start_durations").getList(Long.class);
            if (stored != null) {
                durations = stored;
            }
        } catch (Exception e) {
            OnDemandServerVelocity.getLogger().error(
                    "[StatsCFG] Failed to retrieve start durations for server {}: {}",
                    serverName, e.getMessage(), e
            );
        }
        return durations;
    }

    /**
     * Calculates the average start-up duration from the recorded values.
     *
     * @param serverName unique identifier of the server
     * @return average time in milliseconds, or 0 if no records exist
     */
    public static long getAvgStartDuration(String serverName) {
        List<Long> durations = getLastStartDurations(serverName);
        if (durations.isEmpty()) {
            return 0L;
        }

        long sum = 0L;
        for (Long dur : durations) {
            sum += dur;
        }
        return sum / durations.size();
    }
}
