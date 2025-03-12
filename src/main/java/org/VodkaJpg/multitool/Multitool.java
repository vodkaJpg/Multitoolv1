package org.VodkaJpg.multitool;

import org.VodkaJpg.multitool.commands.CommandManager;
import org.VodkaJpg.multitool.listeners.MultitoolListener;
import org.VodkaJpg.multitool.managers.MessageManager;
import org.VodkaJpg.multitool.utils.ItemUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class Multitool extends JavaPlugin {
    private FileConfiguration config;
    private Random random;
    private CommandManager commandManager;
    private MessageManager messageManager;
    private ItemUtils itemUtils;

    @Override
    public void onEnable() {
        // Zapisz domyślną konfigurację
        saveDefaultConfig();
        config = getConfig();
        random = new Random();
        
        // Inicjalizacja managerów
        messageManager = new MessageManager(this);
        itemUtils = new ItemUtils(this);
        
        // Inicjalizacja CommandManager
        commandManager = new CommandManager(this);
        
        // Zarejestruj nasłuchiwacze
        getServer().getPluginManager().registerEvents(new MultitoolListener(this), this);
        
        // Zarejestruj komendy
        getCommand("multitool").setExecutor(commandManager);
        getCommand("multitool").setTabCompleter(commandManager);
        
        getLogger().info(messageManager.getPrefix() + messageManager.getMessage("plugin.enabled"));
    }

    @Override
    public void onDisable() {
        getLogger().info(messageManager.getPrefix() + messageManager.getMessage("plugin.disabled"));
    }

    public boolean hasChance(double chance) {
        return random.nextDouble() < chance;
    }

    public FileConfiguration getMultitoolConfig() {
        return config;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public ItemUtils getItemUtils() {
        return itemUtils;
    }
}
