package me.skylighteffect.ondemandservervelocity.configs;

import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import java.text.MessageFormat;

public class MainCFG {
    private static ConfigurationNode config;

    public static void init(Logger logger) {
        config = ConfigLoader.loadConfig("config.yml", logger);
    }

    public static String getContent(String path, Object... replace) {
        // Access the node using the modern Configurate API
        ConfigurationNode node = config.node((Object[]) path.split("\\."));
        String content = node.getString();  // Use getString() to retrieve value

        if (content != null && !content.isEmpty()) {
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
        String value = config.node("max_startup_time").getString();  // Use modern node access method
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0L;
    }
}
