package org.VodkaJpg.multitool.utils;

import org.VodkaJpg.multitool.Multitool;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemUtils {
    private final Multitool plugin;

    public ItemUtils(Multitool plugin) {
        this.plugin = plugin;
    }

    public ItemStack createMultitool(int level) {
        ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = tool.getItemMeta();
        
        Map<String, String> replacements = new HashMap<>();
        replacements.put("level", String.valueOf(level));
        replacements.put("efficiency", String.valueOf(plugin.getMultitoolConfig().getInt("multitool.levels." + level + ".efficiency")));
        replacements.put("fortune", String.valueOf(plugin.getMultitoolConfig().getInt("multitool.levels." + level + ".fortune")));
        
        meta.setDisplayName(plugin.getMessageManager().getItemMessage("name", replacements));
        
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getItemMessage("lore.level", replacements));
        lore.add(plugin.getMessageManager().getItemMessage("lore.efficiency", replacements));
        lore.add(plugin.getMessageManager().getItemMessage("lore.fortune", replacements));
        
        List<String> bonuses = plugin.getMultitoolConfig().getStringList("multitool.levels." + level + ".bonuses");
        if (!bonuses.isEmpty()) {
            lore.add("");
            lore.add(plugin.getMessageManager().getItemMessage("lore.bonuses_title"));
            for (String bonus : bonuses) {
                replacements.put("bonus", bonus);
                lore.add(plugin.getMessageManager().getItemMessage("lore.bonus", replacements));
            }
        }
        
        meta.setLore(lore);
        tool.setItemMeta(meta);
        return tool;
    }

    public boolean isMultitool(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return item.getItemMeta().getDisplayName().contains("Multitool");
    }

    public int getMultitoolLevel(ItemStack tool) {
        if (!isMultitool(tool)) return 0;
        String name = tool.getItemMeta().getDisplayName();
        try {
            return Integer.parseInt(name.split("Poziom ")[1]);
        } catch (Exception e) {
            return 0;
        }
    }
} 