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
import java.text.MessageFormat;

public class MsgCFG {
    private static ConfigurationNode config;
    private static final String filename = "messages.yml";

    public static void loadConfig(Path path, ProxyServer proxy, Logger logger) {
        // Determine the path to the target folder ("OnDemandServerVelocity" subfolder)
        File targetFolder = OnDemandServerVelocity.getDataFolder();

        // Determine the path to the target file inside the target folder
        File targetFile = new File(targetFolder, filename);

        // Check if the target file already exists
        if (!targetFile.exists()) {
            // Copy the messages.yml file from the .jar file to the target folder
            try (InputStream inputStream = OnDemandServerVelocity.class.getResourceAsStream("/" + filename)) {
                Files.copy(inputStream, targetFile.toPath());
                System.out.println("The messages.yml file has been successfully copied.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("The messages.yml file already exists in the target folder.");
        }

        // Load config using the new Configurate API
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(targetFile.toPath()) // Using .path() method for file path
                .build();

        try {
            config = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getContent(String path, Object... replace) {
        // Access the node using the modern Configurate API
        ConfigurationNode node = config.node((Object[]) path.split("\\."));
        String content = node.getString();  // Use getString() to retrieve value

        if (content != null && !content.isEmpty()) {
            String formattedContent = MessageFormat.format(content.replace('&', '§'), replace);
            if (!path.equals("prefix")) {
                formattedContent = formattedContent.replace("%PREFIX%", MsgCFG.getContent("prefix"));
            }
            return formattedContent;
        }

        return path;
    }
}
