package me.skylighteffect.ondemandservervelocity.configs;

import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MsgCFG {
    private static ConfigurationNode config;

    public static void init(Logger logger) {
        config = ConfigLoader.loadConfig("messages.yml", logger);
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
