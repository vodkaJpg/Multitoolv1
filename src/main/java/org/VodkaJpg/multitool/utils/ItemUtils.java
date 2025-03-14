package org.VodkaJpg.multitool.utils;

import org.VodkaJpg.multitool.Multitool;
import org.VodkaJpg.multitool.enchants.Enchant;
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
        
        // Dodaj enchanty bezpośrednio
        plugin.getLogger().info("Dodaję enchanty bezpośrednio dla poziomu " + toolLevel);
        
        // Efektywność
        int efficiencyLevel = levelConfig.getInt("efficiency", 0);
        if (efficiencyLevel > 0) {
            meta.addEnchant(Enchantment.EFFICIENCY, efficiencyLevel, true);
            plugin.getLogger().info("Dodano Efficiency " + efficiencyLevel);
        }
        
        // Fortuna
        int fortuneLevel = levelConfig.getInt("fortune", 0);
        if (fortuneLevel > 0) {
            meta.addEnchant(Enchantment.FORTUNE, fortuneLevel, true);
            plugin.getLogger().info("Dodano Fortune " + fortuneLevel);
        }
        
        // Przygotuj lore
        List<String> lore = new ArrayList<>();
        
        // Dodaj sekcję bonusów
        List<String> bonuses = levelConfig.getStringList("bonuses");
        if (!bonuses.isEmpty()) {
            lore.add(plugin.getMessageManager().getItemMessage("lore.bonuses_title"));
            for (String bonus : bonuses) {
                Map<String, String> bonusReplacements = new HashMap<>();
                bonusReplacements.put("bonus", bonus);
                lore.add(plugin.getMessageManager().getItemMessage("lore.bonus", bonusReplacements));
            }
        }
        
        // Dodaj informacje o blokach
        if (!lore.isEmpty()) lore.add("");
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

    public void addCustomEnchant(ItemStack item, Enchant enchant, int level) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();

        // Usuń stary enchant jeśli istnieje
        lore.removeIf(line -> line.contains(enchant.getName()));

        // Dodaj nowy enchant
        lore.add(plugin.getMessageManager().getItemMessage("lore.enchantment", Map.of(
            "enchantment", enchant.getName() + " " + toRomanNumeral(level)
        )));

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public boolean hasCustomEnchant(ItemStack item, Enchant enchant) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;

        return meta.getLore().stream()
                .anyMatch(line -> line.contains(enchant.getName()));
    }

    public int getCustomEnchantLevel(ItemStack item, Enchant enchant) {
        if (!hasCustomEnchant(item, enchant)) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return 0;

        for (String line : meta.getLore()) {
            if (line.contains(enchant.getName())) {
                try {
                    String levelStr = line.split(enchant.getName() + " ")[1];
                    return Utils.romanNumeralToInt(levelStr);
                } catch (Exception e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public static boolean hasEnchant(ItemStack item, Enchant enchant) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return false;
        }
        
        for (String line : item.getItemMeta().getLore()) {
            String strippedLine = ChatColor.stripColor(line);
            if (strippedLine.startsWith(enchant.getName())) {
                return true;
            }
        }
        return false;
    }
} 