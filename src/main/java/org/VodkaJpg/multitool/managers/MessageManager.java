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
    private final FileConfiguration messages;

    public MessageManager(FileConfiguration messages) {
        this.messages = messages;
    }

    public String getMessage(String path) {
        String message = messages.getString("messages." + path);
        if (message == null) {
            return "§cNie znaleziono wiadomości: messages." + path;
        }
        return colorize(message);
    }

    public String getMessage(String path, Map<String, String> replacements) {
        String message = getMessage(path);
        if (replacements != null) {
            for (Map.Entry<String, String> replacement : replacements.entrySet()) {
                message = message.replace("%" + replacement.getKey() + "%", replacement.getValue());
                message = message.replace("{" + replacement.getKey() + "}", replacement.getValue());
            }
        }
        return message;
    }

    public String getSuccess(String path) {
        return getMessage("success." + path);
    }

    public String getSuccess(String path, Map<String, String> replacements) {
        return getMessage("success." + path, replacements);
    }

    public String getPrefix() {
        return getMessage("prefix");
    }

    public String getCommandMessage(String path) {
        return getMessage("commands." + path);
    }

    public String getCommandMessage(String path, Map<String, String> replacements) {
        return getMessage("commands." + path, replacements);
    }

    public String getError(String path) {
        return getMessage("errors." + path);
    }

    public String getItemMessage(String path) {
        return getMessage("item." + path);
    }

    public String getItemMessage(String path, Map<String, String> replacements) {
        return getMessage("item." + path, replacements);
    }

    private String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
} 