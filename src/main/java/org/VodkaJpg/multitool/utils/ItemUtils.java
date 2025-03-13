package org.VodkaJpg.multitool.utils;

import org.VodkaJpg.multitool.Multitool;
import org.bukkit.Material;
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

    public ItemStack createMultitool(int level) {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        
        // Ustaw nazwę przedmiotu
        Map<String, String> nameReplacements = new HashMap<>();
        nameReplacements.put("level", String.valueOf(level));
        meta.setDisplayName(plugin.getMessageManager().getItemMessage("name", nameReplacements));
        
        // Pobierz konfigurację dla danego poziomu
        ConfigurationSection levelConfig = plugin.getMultitoolConfig().getConfigurationSection("levels." + level);
        if (levelConfig == null) return item;
        
        // Pobierz enchanty
        List<String> enchantments = levelConfig.getStringList("enchantments");
        for (String enchant : enchantments) {
            String[] parts = enchant.split(":");
            if (parts.length == 2) {
                Enchantment enchantment = Enchantment.getByName(parts[0]);
                if (enchantment != null) {
                    meta.addEnchant(enchantment, Integer.parseInt(parts[1]), true);
                }
            }
        }
        
        // Pobierz bonusy
        List<String> bonuses = levelConfig.getStringList("bonuses");
        
        // Pobierz wymagane bloki
        long requiredBlocks = levelConfig.getLong("required_blocks", 0);
        
        // Ustaw lore
        List<String> lore = new ArrayList<>();
        
        // Dodaj poziom
        Map<String, String> levelReplacements = new HashMap<>();
        levelReplacements.put("level", String.valueOf(level));
        lore.add(plugin.getMessageManager().getItemMessage("lore.level", levelReplacements));
        
        // Dodaj wydajność
        Map<String, String> efficiencyReplacements = new HashMap<>();
        efficiencyReplacements.put("efficiency", String.valueOf(levelConfig.getInt("efficiency")));
        lore.add(plugin.getMessageManager().getItemMessage("lore.efficiency", efficiencyReplacements));
        
        // Dodaj fortunę
        Map<String, String> fortuneReplacements = new HashMap<>();
        fortuneReplacements.put("fortune", String.valueOf(levelConfig.getInt("fortune")));
        lore.add(plugin.getMessageManager().getItemMessage("lore.fortune", fortuneReplacements));
        
        // Dodaj wymagane bloki
        lore.add("&7Wymagane bloki do następnego poziomu: &e" + formatNumber(requiredBlocks));
        
        // Dodaj bonusy
        if (!bonuses.isEmpty()) {
            lore.add("");
            lore.add(plugin.getMessageManager().getItemMessage("lore.bonuses_title"));
            for (String bonus : bonuses) {
                Map<String, String> bonusReplacements = new HashMap<>();
                bonusReplacements.put("bonus", bonus);
                lore.add(plugin.getMessageManager().getItemMessage("lore.bonus", bonusReplacements));
            }
        }
        
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        
        return item;
    }

    public boolean isMultitool(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_PICKAXE) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        return meta.getDisplayName().contains("Multitool");
    }

    public int getMultitoolLevel(ItemStack item) {
        if (!isMultitool(item)) return 0;
        String name = item.getItemMeta().getDisplayName();
        try {
            return Integer.parseInt(name.split("Poziom ")[1].replace("§", ""));
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