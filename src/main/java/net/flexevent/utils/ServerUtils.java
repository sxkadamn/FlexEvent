package net.flexevent.utils;

import net.flexevent.FlexEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;

public class ServerUtils {


    private final FlexEvent flexEvent;

    public ServerUtils(FlexEvent flexEvent) {
        this.flexEvent = flexEvent;
    }

    public void broadcastMessage(String key, String... placeholders) {
        Bukkit.getOnlinePlayers().forEach(player ->
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        getConfigString(key, placeholders))));
    }

    public String getConfigString(String key, String... placeholders) {
        String message = flexEvent.getConfig().getString(key, "");
        return replacePlaceholders(message, placeholders);
    }

    public int getConfigInt(String key) {
        return flexEvent.getConfig().getInt(key, 0);
    }

    public List<String> getConfigStringList(String key) {
        return flexEvent.getConfig().getStringList(key);
    }

    public String replacePlaceholders(String message, String... placeholders) {
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        return message;
    }
}
