package me.skylighteffect.ondemandservervelocity.configs;

import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * {@code MsgCFG} handles all message-related configuration loading and formatting for the plugin.
 * <p>
 * It loads data from {@code messages.yml} and provides access to localized, formatted message
 * strings that can be used throughout the plugin, including support for configurable prefixes,
 * color codes, and dynamic argument replacement.
 *
 * @author SkyLightEffect
 */
public class MsgCFG {

    /**
     * Root node of the loaded {@code messages.yml} configuration.
     */
    private static ConfigurationNode config;

    /**
     * Initializes the message configuration by loading {@code messages.yml} from the plugin's config directory.
     *
     * @param logger the logger instance for reporting issues during configuration loading
     */
    public static void init(Logger logger) {
        config = ConfigLoader.loadConfig("messages.yml", logger);
    }

    /**
     * Retrieves and formats a string from the configuration based on the provided path.
     * Supports placeholder replacements using {@link java.text.MessageFormat} and inserts
     * a global prefix if present.
     *
     * @param path    the dot-separated path in the configuration (e.g., "errors.not_found")
     * @param replace optional replacement parameters to inject into the string
     * @return the formatted string, or the raw path if not found or empty
     */
    public static String getContent(String path, Object... replace) {
        ConfigurationNode node = config.node((Object[]) path.split("\\."));
        String content = node.getString();

        if (content != null && !content.isEmpty()) {
            String formattedContent = MessageFormat.format(content.replace('&', '§'), replace);
            if (!path.equals("prefix")) {
                formattedContent = formattedContent.replace("%PREFIX%", MsgCFG.getContent("prefix"));
            }
            return formattedContent;
        }

        return path; // fallback to path if message is missing
    }

    /**
     * Enumeration of countdown types used to retrieve randomized wait messages.
     */
    public enum CountdownWait {
        LONG,
        MEDIUM,
        SHORT
    }

    /**
     * Retrieves a random countdown wait message from the configuration for the specified wait type.
     * <p>
     * Messages are expected under the configuration path: {@code countdown.[type]_wait}, where
     * type is lowercase (e.g., {@code countdown.long_wait}).
     *
     * @param countdownWait the type of countdown (LONG, MEDIUM, SHORT)
     * @param replace       optional placeholders for the message
     * @return a formatted countdown message or {@code null} if none found
     */
    public static String getRandomLongWaitMessage(CountdownWait countdownWait, Object... replace) {
        List<String> messages = new ArrayList<>();
        try {
            ConfigurationNode serverNode = config.node("countdown");
            messages = serverNode.node(countdownWait.toString().toLowerCase() + "_wait").getList(String.class);

            if (messages == null || messages.isEmpty()) {
                return null;
            }

            Random generator = new Random();
            String content = messages.get(generator.nextInt(messages.size()));
            String formattedContent = MessageFormat.format(content.replace('&', '§'), replace);
            return formattedContent.replace("%PREFIX%", MsgCFG.getContent("prefix"));
        } catch (Exception e) {
            e.printStackTrace(); // Consider logging this in production instead of printing stack trace
        }
        return null;
    }
}
