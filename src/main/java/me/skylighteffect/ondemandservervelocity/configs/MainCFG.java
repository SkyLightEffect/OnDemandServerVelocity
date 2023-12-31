package me.skylighteffect.ondemandservervelocity.configs;

import com.velocitypowered.api.proxy.ProxyServer;
import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

public class MainCFG {
    private static ConfigurationNode config;
    private static final String filename = "config.yml";

    public static void loadConfig(Path path, ProxyServer proxy, Logger logger) {
        // Determine the path to the target folder ("OnDemandServerVelocity" subfolder)
        File targetFolder = OnDemandServerVelocity.getDataFolder();

        // Check if the target folder already exists
        if (!targetFolder.exists()) {
            // Create the target folder if it doesn't exist
            // targetFolder.mkdirs();
        }

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

        // Load config
        ConfigurationLoader<?> loader = YAMLConfigurationLoader.builder().setFile(targetFile).build();
        try {
            config = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getContent(String path, Object... replace) {
        String content = config.getNode((Object[]) path.split("\\.")).getString();

        if (content != null && !content.equals("")) {
            String s = MessageFormat.format(content.replace('&', '§'), replace);
            if (!path.equals("prefix")) {
                s = s.replace("%PREFIX%", MsgCFG.getContent("prefix"));
            }
            return s;
        }

        return path;
    }

    public static String getScriptPath() {
        return getContent("start_scripts_path");
    }

    public static long getMaxStartupTimeMillis() {
        String value = config.getNode("max_startup_time").getString();
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                // Error handling if the value cannot be converted to a long
                e.printStackTrace();
            }
        }
        // Default value to be returned if the value is not found or cannot be converted
        return 0L;
    }
}
