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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    public enum CountdownWait {
        LONG,
        MEDIUM,
        SHORT
    }

    public static String getRandomLongWaitMessage(CountdownWait countdownWait, Object... replace) {
        List<String> messages = new ArrayList<>();
        try {
            ConfigurationNode serverNode = config.node("countdown");
            messages = serverNode.node(countdownWait.toString().toLowerCase() + "_wait").getList(String.class);

            if (messages == null) {
                return null;
            } else {
                int size = messages.size();
                Random generator = new Random();
                String content = messages.get(generator.nextInt(size));

                String formattedContent = MessageFormat.format(content.replace('&', '§'), replace);
                formattedContent = formattedContent.replace("%PREFIX%", MsgCFG.getContent("prefix"));

                return formattedContent;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
