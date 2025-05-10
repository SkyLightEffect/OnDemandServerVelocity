package me.skylighteffect.ondemandservervelocity.configs;

import me.skylighteffect.ondemandservervelocity.OnDemandServerVelocity;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import java.text.MessageFormat;

/**
 * {@code MainCFG} manages the primary plugin configuration loaded from config.yml.
 * <p>
 * It provides methods to retrieve formatted strings and key configuration values,
 * handling placeholder replacement, color code translation, and defaults.
 * </p>
 *
 * @author SkyLightEffect
 */
public class MainCFG {

    /**
     * Root configuration node for config.yml.
     */
    private static ConfigurationNode config;

    /**
     * Loads or creates the main configuration file.
     *
     * @param logger SLF4J logger for reporting load errors
     */
    public static void init(Logger logger) {
        config = ConfigLoader.loadConfig("config.yml", logger);
    }

    /**
     * Retrieves and formats a string from the configuration.
     * Applies color code ('&' to '§') conversion and global prefix substitution.
     *
     * @param path    dot-separated path within config.yml (e.g., "messages.welcome")
     * @param replace optional parameters for {@link MessageFormat}
     * @return formatted string or the raw path if missing
     */
    public static String getContent(String path, Object... replace) {
        ConfigurationNode node = config.node((Object[]) path.split("\\."));
        String content = node.getString();

        if (content != null && !content.isEmpty()) {
            String formatted = MessageFormat.format(content.replace('&', '§'), replace);
            if (!"prefix".equals(path)) {
                formatted = formatted.replace("%PREFIX%", MsgCFG.getContent("prefix"));
            }
            return formatted;
        }
        return path;
    }

    /**
     * @return the configured path or command used to start Minecraft server processes.
     */
    public static String getScriptPath() {
        return getContent("start_scripts_path");
    }

    /**
     * Retrieves the maximum allowed startup time for backend servers.
     *
     * @return maximum startup duration in milliseconds; 0 if not configured or invalid
     */
    public static long getMaxStartupTimeMillis() {
        String value = config.node("max_startup_time").getString();
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                OnDemandServerVelocity.getLogger().error(
                        "[MainCFG] Invalid number format for 'max_startup_time': {}", value, e);
            }
        }
        return 0L;
    }
}