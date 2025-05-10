package me.skylighteffect.ondemandservervelocity.configs;

import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigLoader {
    public static ConfigurationNode loadConfig(String filename, Logger logger) {
        // Determine the path to the target folder ("OnDemandServerVelocity" subfolder)
        File targetFolder = OnDemandServerVelocity.getDataFolder();

        // Determine the path to the target file inside the target folder
        File targetFile = new File(targetFolder, filename);

        // Check if the target file already exists
        if (!targetFile.exists()) {
            // Copy the messages.yml file from the .jar file to the target folder
            try (InputStream inputStream = OnDemandServerVelocity.class.getResourceAsStream("/" + filename)) {
                assert inputStream != null;
                Files.copy(inputStream, targetFile.toPath());
                System.out.println("The messages.yml file has been successfully copied.");
            } catch (IOException e) {
                logger.error("Failed to load configuration from {}", targetFile.getPath(), e);
            }
        } else {
            System.out.println("The messages.yml file already exists in the target folder.");
        }

        // Load config using the new Configurate API
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(targetFile.toPath()) // Using .path() method for file path
                .build();

        try {
            return loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Failed to load configuration from {}", targetFile.getPath(), e);
        }

        return null;
    }
}
