package org.VodkaJpg.multitool.utils;

import org.VodkaJpg.multitool.Multitool;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.ChatColor;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class ItemUtils {
    private final Multitool plugin;
    private final NamespacedKey toolIdKey;
    private final NamespacedKey blocksMineKey;

    public ItemUtils(Multitool plugin) {
        this.plugin = plugin;
        this.toolIdKey = new NamespacedKey(plugin, "tool_id");
        this.blocksMineKey = new NamespacedKey(plugin, "blocks_mined");
    }

    public ItemStack createMultitool(int level) {
        return createMultitool(level, 0);
    }

    public ItemStack createMultitool(int toolLevel, long blocksMined) {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        // Dodaj unikalny identyfikator narzędzia
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String toolId = UUID.randomUUID().toString();
        container.set(toolIdKey, PersistentDataType.STRING, toolId);
        container.set(blocksMineKey, PersistentDataType.LONG, blocksMined);
        
        // Ustaw nazwę przedmiotu
        Map<String, String> nameReplacements = new HashMap<>();
        nameReplacements.put("level", String.valueOf(toolLevel));
        meta.setDisplayName(plugin.getMessageManager().getItemMessage("name", nameReplacements));
        
        // Pobierz konfigurację dla danego poziomu
        ConfigurationSection levelConfig = plugin.getMultitoolConfig().getConfigurationSection("levels." + toolLevel);
        if (levelConfig == null) return item;
        
        List<String> lore = new ArrayList<>();
        
        // Logowanie debugowania dla enchantu
        plugin.getLogger().info("Tworzenie Multitool na poziomie " + toolLevel);
        
        // Dodaj sekcję enchantów
        lore.add(plugin.getMessageManager().getItemMessage("lore.enchantments_title"));
        List<String> enchantments = levelConfig.getStringList("enchantments");
        plugin.getLogger().info("Znalezione enchanty: " + enchantments.toString());
        
        for (String enchant : enchantments) {
            String[] parts = enchant.split(":");
            if (parts.length == 3 && parts[0].equals("minecraft")) {
                String enchantName = parts[1];
                try {
                    int enchantLevel = Integer.parseInt(parts[2]);
                    plugin.getLogger().info("Próba dodania enchantu: " + enchantName + " poziom " + enchantLevel);
                    
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName));
                    if (enchantment != null) {
                        // Dodaj enchant do przedmiotu
                        boolean added = meta.addEnchant(enchantment, enchantLevel, true);
                        
                        // Dodaj informacje o enchancie do lore
                        Map<String, String> enchantReplacements = new HashMap<>();
                        String displayName = enchantName.substring(0, 1).toUpperCase() + enchantName.substring(1).toLowerCase();
                        enchantReplacements.put("enchantment", displayName + " " + toRomanNumeral(enchantLevel));
                        lore.add(plugin.getMessageManager().getItemMessage("lore.enchantment", enchantReplacements));
                        plugin.getLogger().info("Dodano enchant: " + enchantName + " poziom " + enchantLevel + " (sukces: " + added + ")");
                    } else {
                        plugin.getLogger().warning("Nie znaleziono enchantu: " + enchantName);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Nieprawidłowy poziom enchantu: " + enchant);
                }
            }
        }
        
        // Dodaj sekcję bonusów
        List<String> bonuses = levelConfig.getStringList("bonuses");
        if (!bonuses.isEmpty()) {
            lore.add("");
            lore.add(plugin.getMessageManager().getItemMessage("lore.bonuses_title"));
            for (String bonus : bonuses) {
                Map<String, String> bonusReplacements = new HashMap<>();
                bonusReplacements.put("bonus", bonus);
                lore.add(plugin.getMessageManager().getItemMessage("lore.bonus", bonusReplacements));
            }
        }
        
        // Dodaj informacje o blokach
        lore.add("");
        Map<String, String> blocksReplacements = new HashMap<>();
        blocksReplacements.put("blocks", formatNumber(blocksMined));
        blocksReplacements.put("required_blocks", formatNumber(levelConfig.getLong("required_blocks")));
        lore.add(plugin.getMessageManager().getItemMessage("lore.blocks_mined", blocksReplacements));
        
        // Dodaj informację o łącznej ilości bloków
        Map<String, String> totalBlocksReplacements = new HashMap<>();
        long totalBlocks = calculateTotalBlocks(toolLevel - 1) + blocksMined;
        totalBlocksReplacements.put("total_blocks", formatNumber(totalBlocks));
        lore.add(plugin.getMessageManager().getItemMessage("lore.total_blocks", totalBlocksReplacements));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        // Sprawdź czy enchanty zostały dodane
        plugin.getLogger().info("Utworzono Multitool z enchantami: " + item.getEnchantments().toString());
        
        return item;
    }

    public void updateBlocksMined(ItemStack item, long newBlocksMined) {
        if (!isMultitool(item)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(blocksMineKey, PersistentDataType.LONG, newBlocksMined);
        
        int level = getMultitoolLevel(item);
        ItemStack updatedItem = createMultitool(level, newBlocksMined);
        item.setItemMeta(updatedItem.getItemMeta());
    }

    public long getBlocksMined(ItemStack item) {
        if (!isMultitool(item)) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        Long blocksMined = container.get(blocksMineKey, PersistentDataType.LONG);
        return blocksMined != null ? blocksMined : 0;
    }

    public String getToolId(ItemStack item) {
        if (!isMultitool(item)) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(toolIdKey, PersistentDataType.STRING);
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

    private String toRomanNumeral(int number) {
        if (number < 1 || number > 3999) {
            throw new IllegalArgumentException("Number out of range (1-3999)");
        }

        StringBuilder roman = new StringBuilder();
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] numerals = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                number -= values[i];
                roman.append(numerals[i]);
            }
        }

        return roman.toString();
    }
} 