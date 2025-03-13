package org.VodkaJpg.multitool.managers;

import org.VodkaJpg.multitool.Multitool;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final Plugin plugin;
    private FileConfiguration messages;
    private final Map<String, String> cache = new HashMap<>();

    public MessageManager(Plugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        cache.clear();
    }

    public String getMessage(String path) {
        return getMessage(path, new HashMap<>());
    }

    public String getMessage(String path, Map<String, String> replacements) {
        String message = messages.getString(path);
        if (message == null) {
            return "Message not found: " + path;
        }

        message = ChatColor.translateAlternateColorCodes('&', message);
        
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return message;
    }

    public String getPrefix() {
        String prefix = messages.getString("messages.prefix");
        if (prefix == null) {
            return "&6[Multitool]";
        }
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }

    public String getCommandMessage(String path) {
        return getMessage("messages.commands." + path);
    }

    public String getError(String path) {
        return getMessage("messages.errors." + path);
    }

    public String getSuccess(String path) {
        return getMessage("messages.success." + path);
    }

    public String getSuccess(String path, Map<String, String> replacements) {
        return getMessage("messages.success." + path, replacements);
    }

    public String getItemMessage(String path) {
        return getMessage("messages.item." + path);
    }

    public String getItemMessage(String path, Map<String, String> replacements) {
        return getMessage("messages.item." + path, replacements);
    }

    private String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
} 