package org.VodkaJpg.multitool.utils;

import org.VodkaJpg.multitool.Multitool;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ItemUtils {
    private final Multitool plugin;

    public ItemUtils(Multitool plugin) {
        this.plugin = plugin;
    }

    public ItemStack createMultitool(int level, long blocksMined) {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        
        // Ustaw nazwę przedmiotu
        Map<String, String> nameReplacements = new HashMap<>();
        nameReplacements.put("level", String.valueOf(level));
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("messages.item.name", nameReplacements)));
        
        // Pobierz konfigurację dla danego poziomu
        ConfigurationSection levelConfig = plugin.getMultitoolConfig().getConfigurationSection("levels." + level);
        if (levelConfig == null) return item;
        
        // Pobierz enchanty
        List<String> enchantments = levelConfig.getStringList("enchantments");
        
        // Pobierz bonusy
        List<String> bonuses = levelConfig.getStringList("bonuses");
        
        // Pobierz wymagane bloki
        long requiredBlocks = levelConfig.getLong("required_blocks", 0);
        
        // Ustaw lore
        List<String> lore = new ArrayList<>();
        
        // Dodaj sekcję enchantów
        lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("messages.item.lore.enchantments_title")));
        for (String enchant : enchantments) {
            String[] parts = enchant.split(":");
            if (parts.length == 2) {
                Enchantment enchantment = Enchantment.getByName(parts[0]);
                if (enchantment != null) {
                    meta.addEnchant(enchantment, Integer.parseInt(parts[1]), true);
                    Map<String, String> enchantReplacements = new HashMap<>();
                    String enchantName = enchantment.getKey().getKey().toLowerCase();
                    enchantName = enchantName.substring(0, 1).toUpperCase() + enchantName.substring(1);
                    enchantReplacements.put("enchantment", enchantName + " " + parts[1]);
                    lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("messages.item.lore.enchantment", enchantReplacements)));
                }
            }
        }
        
        // Dodaj sekcję bonusów
        if (!bonuses.isEmpty()) {
            lore.add("");
            lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("messages.item.lore.bonuses_title")));
            for (String bonus : bonuses) {
                Map<String, String> bonusReplacements = new HashMap<>();
                bonusReplacements.put("bonus", bonus);
                lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("messages.item.lore.bonus", bonusReplacements)));
            }
        }
        
        // Dodaj poziom
        lore.add("");
        Map<String, String> levelReplacements = new HashMap<>();
        levelReplacements.put("level", String.valueOf(level));
        lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("messages.item.lore.level", levelReplacements)));
        
        // Dodaj informacje o blokach
        Map<String, String> blocksReplacements = new HashMap<>();
        blocksReplacements.put("blocks", formatNumber(blocksMined));
        blocksReplacements.put("required_blocks", formatNumber(requiredBlocks));
        lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("messages.item.lore.blocks_mined", blocksReplacements)));
        
        // Dodaj informację o łącznej ilości bloków
        lore.add("");
        Map<String, String> totalBlocksReplacements = new HashMap<>();
        // Oblicz łączną ilość bloków z poprzednich poziomów
        long totalBlocks = calculateTotalBlocks(level - 1) + blocksMined;
        totalBlocksReplacements.put("total_blocks", formatNumber(totalBlocks));
        lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("messages.item.lore.total_blocks", totalBlocksReplacements)));
        
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        
        return item;
    }

    private long calculateTotalBlocks(int level) {
        long total = 0;
        for (int i = 1; i <= level; i++) {
            ConfigurationSection levelConfig = plugin.getMultitoolConfig().getConfigurationSection("levels." + i);
            if (levelConfig != null) {
                total += levelConfig.getLong("required_blocks", 0);
            }
        }
        return total;
    }

    public ItemStack createMultitool(int level) {
        return createMultitool(level, 0);
    }

    public boolean isMultitool(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_PICKAXE) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        return meta.getDisplayName().contains("MultiTool");
    }

    public int getMultitoolLevel(ItemStack item) {
        if (!isMultitool(item)) return 0;
        String name = item.getItemMeta().getDisplayName();
        try {
            return Integer.parseInt(name.split("Poziom ")[1].replace("§", "").replace("]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatNumber(long number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        }
        return String.valueOf(number);
    }
} 