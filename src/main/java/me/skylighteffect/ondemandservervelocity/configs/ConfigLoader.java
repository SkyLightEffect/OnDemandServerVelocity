package me.skylighteffect.ondemandservervelocity.configs;

import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * {@code ConfigLoader} handles the creation and loading of YAML-based configuration files
 * within the plugin's data directory. On first run, it copies default resources from the JAR.
 *
 * @author SkyLightEffect
 */
public class ConfigLoader {

    /**
     * Loads a YAML configuration file, copying a default resource from the JAR if needed.
     *
     * @param filename name of the config file (e.g., "messages.yml")
     * @param logger   SLF4J logger for error reporting
     * @return the loaded {@link ConfigurationNode}, or {@code null} on failure
     */
    public static ConfigurationNode loadConfig(String filename, Logger logger) {
        File dataDir = OnDemandServerVelocity.getDataFolder();
        File targetFile = new File(dataDir, filename);

        // Copy default resource if missing
        if (!targetFile.exists()) {
            try (InputStream in = OnDemandServerVelocity.class.getResourceAsStream("/" + filename)) {
                if (in == null) {
                    logger.error("[ConfigLoader] Resource '{}' not found inside JAR.", filename);
                } else {
                    Files.copy(in, targetFile.toPath());
                    logger.info("[ConfigLoader] Created default {} in {}.", filename, dataDir);
                }
            } catch (IOException e) {
                logger.error("[ConfigLoader] Failed to copy {} to {}.", filename, targetFile.getPath(), e);
            }
        } else {
            logger.debug("[ConfigLoader] Using existing config: {}", targetFile.getPath());
        }

        // Load YAML configuration
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(targetFile.toPath())
                .build();
        try {
            return loader.load();
        } catch (IOException e) {
            logger.error("[ConfigLoader] Failed to load {}.", targetFile.getPath(), e);
            return null;
        }
    }
}

