package org.VodkaJpg.multitool;

import org.VodkaJpg.multitool.commands.CommandManager;
import org.VodkaJpg.multitool.listeners.MultitoolListener;
import org.VodkaJpg.multitool.managers.MessageManager;
import org.VodkaJpg.multitool.utils.ItemUtils;

import org.VodkaJpg.multitool.enchants.TelekineticEnchant;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Random;

public class Multitool extends JavaPlugin {
    private static Multitool instance;
    private FileConfiguration config;
    private FileConfiguration messagesConfig;
    private Random random;
    private CommandManager commandManager;
    private MessageManager messageManager;
    private ItemUtils itemUtils;
 
    private TelekineticEnchant telekineticEnchant;

    @Override
    public void onEnable() {
        instance = this;
        
        // Zarejestruj enchant Auto-Smelt
        

        // Zarejestruj enchant Lifesteal
        
        // Zarejestruj enchant Telekinetic
        telekineticEnchant = new TelekineticEnchant();
        getLogger().info("Pomyślnie zarejestrowano enchant Telekinetic");
        
        // Zapisz domyślne pliki konfiguracyjne
        saveDefaultConfig();
        saveResource("messages.yml", false);
        
        // Załaduj konfiguracje
        reloadConfigs();
        
        // Inicjalizacja menedżerów
        random = new Random();
        messageManager = new MessageManager(messagesConfig);
        itemUtils = new ItemUtils(this);
        
        // Inicjalizacja CommandManager
        commandManager = new CommandManager(this);
        
        // Zarejestruj nasłuchiwacze
        getServer().getPluginManager().registerEvents(new MultitoolListener(this), this);
        getServer().getPluginManager().registerEvents(telekineticEnchant, this);
        
        // Zarejestruj komendy
        getCommand("multitool").setExecutor(commandManager);
        getCommand("multitool").setTabCompleter(commandManager);
        
        // Log włączenia pluginu
        getLogger().info(messageManager.getPrefix() + " " + messageManager.getMessage("messages.plugin.enabled"));
    }

    @Override
    public void onDisable() {
        
        
        if (messageManager != null) {
            getLogger().info(messageManager.getPrefix() + " " + messageManager.getMessage("messages.plugin.disabled"));
        } else {
            getLogger().info("Plugin został wyłączony!");
        }
    }

    public void reloadConfigs() {
        // Przeładuj config.yml
        reloadConfig();
        config = getConfig();
        
        // Przeładuj messages.yml
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Załaduj domyślne wiadomości z zasobów jako fallback
        InputStream defaultMessagesStream = getResource("messages.yml");
        if (defaultMessagesStream != null) {
            YamlConfiguration defaultMessages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
            messagesConfig.setDefaults(defaultMessages);
        }
        
        // Jeśli MessageManager już istnieje, zaktualizuj go
        if (messageManager != null) {
            messageManager = new MessageManager(messagesConfig);
        }
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

    public static Multitool getInstance() {
        return instance;
    }

    public Random getRandom() {
        return random;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }


    public TelekineticEnchant getTelekineticEnchant() {
        return telekineticEnchant;
    }

}