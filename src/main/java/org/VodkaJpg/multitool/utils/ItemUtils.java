package org.VodkaJpg.multitool.utils;

import org.VodkaJpg.multitool.Multitool;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ItemUtils {
    private final Multitool plugin;

    public ItemUtils(Multitool plugin) {
        this.plugin = plugin;
    }

    public ItemStack createMultitool(int toolLevel, long blocksMined) {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        
        // Ustaw nazwę przedmiotu
        Map<String, String> nameReplacements = new HashMap<>();
        nameReplacements.put("level", String.valueOf(toolLevel));
        meta.setDisplayName(plugin.getMessageManager().getItemMessage("name", nameReplacements));
        
        // Pobierz konfigurację dla danego poziomu
        ConfigurationSection levelConfig = plugin.getMultitoolConfig().getConfigurationSection("levels." + toolLevel);
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
        if (!enchantments.isEmpty()) {
            lore.add(plugin.getMessageManager().getItemMessage("lore.enchantments_title"));
            for (String enchant : enchantments) {
                String[] parts = enchant.split(":");
                if (parts.length == 2) {
                    String enchantName = parts[0].toUpperCase();
                    try {
                        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
                        if (enchantment != null) {
                            int enchantLevel = Integer.parseInt(parts[1]);
                            meta.addEnchant(enchantment, enchantLevel, true);
                            
                            Map<String, String> enchantReplacements = new HashMap<>();
                            // Formatuj nazwę enchantu (np. z "efficiency" na "Efficiency")
                            String formattedName = enchantName.substring(0, 1).toUpperCase() + 
                                                 enchantName.substring(1).toLowerCase();
                            enchantReplacements.put("enchantment", formattedName + " " + enchantLevel);
                            lore.add(plugin.getMessageManager().getItemMessage("lore.enchantment", enchantReplacements));
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Nieprawidłowy enchant: " + enchantName);
                    }
                }
            }
        }
        
        // Dodaj sekcję bonusów
        if (!bonuses.isEmpty()) {
            lore.add("");
            lore.add(plugin.getMessageManager().getItemMessage("lore.bonuses_title"));
            for (String bonus : bonuses) {
                Map<String, String> bonusReplacements = new HashMap<>();
                bonusReplacements.put("bonus", bonus);
                lore.add(plugin.getMessageManager().getItemMessage("lore.bonus", bonusReplacements));
            }
        }
        
        // Dodaj poziom
        lore.add("");
        Map<String, String> levelReplacements = new HashMap<>();
        levelReplacements.put("level", String.valueOf(toolLevel));
        lore.add(plugin.getMessageManager().getItemMessage("lore.level", levelReplacements));
        
        // Dodaj informacje o blokach
        Map<String, String> blocksReplacements = new HashMap<>();
        blocksReplacements.put("blocks", formatNumber(blocksMined));
        blocksReplacements.put("required_blocks", formatNumber(requiredBlocks));
        lore.add(plugin.getMessageManager().getItemMessage("lore.blocks_mined", blocksReplacements));
        
        // Dodaj informację o łącznej ilości bloków
        lore.add("");
        Map<String, String> totalBlocksReplacements = new HashMap<>();
        long totalBlocks = calculateTotalBlocks(toolLevel - 1) + blocksMined;
        totalBlocksReplacements.put("total_blocks", formatNumber(totalBlocks));
        lore.add(plugin.getMessageManager().getItemMessage("lore.total_blocks", totalBlocksReplacements));
        
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
            return Integer.parseInt(name.split("Poziom ")[1].replace("]", ""));
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