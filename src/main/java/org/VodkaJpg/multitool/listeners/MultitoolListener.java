package org.VodkaJpg.multitool.listeners;

import org.VodkaJpg.multitool.Multitool;
import org.VodkaJpg.multitool.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class MultitoolListener implements Listener {
    private final Multitool plugin;

    public MultitoolListener(Multitool plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (plugin.getItemUtils().isMultitool(item)) {
            // Zmień typ narzędzia w zależności od bloku
            changeToolType(item, event.getBlock());
            
            // Pobierz aktualne statystyki narzędzia
            long blocksMined = plugin.getItemUtils().getBlocksMined(item);
            int currentLevel = plugin.getItemUtils().getMultitoolLevel(item);
            
            // Zwiększ licznik wykopanych bloków
            blocksMined++;
            
            // Sprawdź, czy narzędzie powinno zostać ulepszone
            ConfigurationSection levelConfig = plugin.getMultitoolConfig().getConfigurationSection("levels." + currentLevel);
            if (levelConfig != null) {
                long requiredBlocks = levelConfig.getLong("required_blocks", 0);
                if (blocksMined >= requiredBlocks && currentLevel < 7) {
                    // Utwórz nowe narzędzie o wyższym poziomie
                    ItemStack newTool = plugin.getItemUtils().createMultitool(currentLevel + 1, 0);
                    player.getInventory().setItemInMainHand(newTool);
                    
                    // Wyślij wiadomość o ulepszeniu
                    Map<String, String> replacements = new HashMap<>();
                    replacements.put("level", String.valueOf(currentLevel + 1));
                    player.sendMessage(plugin.getMessageManager().getMessage("success.level_up", replacements));
                } else {
                    // Zaktualizuj statystyki narzędzia
                    plugin.getItemUtils().updateBlocksMined(item, blocksMined);
                }
            }
            
            // Zastosuj bonusy w zależności od poziomu
            applyBonuses(event);
        }
    }

    private void changeToolType(ItemStack tool, Block block) {
        Material newType = getAppropriateToolType(block.getType());
        if (newType != tool.getType()) {
            tool.setType(newType);
        }
    }

    private Material getAppropriateToolType(Material blockType) {
        if (blockType.name().contains("_ORE") || blockType.name().contains("_STONE")) {
            return Material.DIAMOND_PICKAXE;
        } else if (blockType.name().contains("_DIRT") || blockType.name().contains("_SAND")) {
            return Material.DIAMOND_SHOVEL;
        } else if (blockType.name().contains("_LOG") || blockType.name().contains("_LEAVES")) {
            return Material.DIAMOND_AXE;
        } else if (blockType.name().contains("_GRASS") || blockType.name().contains("_PLANT")) {
            return Material.DIAMOND_HOE;
        }
        return Material.DIAMOND_PICKAXE;
    }

    private void applyBonuses(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        int level = plugin.getItemUtils().getMultitoolLevel(tool);
        Block block = event.getBlock();
        
        // Pobierz konfigurację dla danego poziomu
        ConfigurationSection levelConfig = plugin.getMultitoolConfig().getConfigurationSection("levels." + level);
        if (levelConfig == null) return;

        // Sprawdź bonusy dla danego poziomu
        if (level >= 3 && block.getType() == Material.IRON_ORE && plugin.hasChance(0.001)) {
            event.getBlock().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.IRON_BLOCK));
        }
        if (level >= 4 && block.getType() == Material.GOLD_ORE && plugin.hasChance(0.001)) {
            event.getBlock().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GOLD_BLOCK));
        }
        if (level >= 5) {
            if (block.getType() == Material.IRON_ORE) {
                event.setDropItems(false);
                event.setExpToDrop(0);
                event.getBlock().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.IRON_INGOT));
            } else if (block.getType() == Material.GOLD_ORE) {
                event.setDropItems(false);
                event.setExpToDrop(0);
                event.getBlock().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GOLD_INGOT));
            }
        }
        if (level >= 6 && block.getType() == Material.DIAMOND_ORE && plugin.hasChance(0.001)) {
            event.getBlock().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.DIAMOND_BLOCK));
        }
        if (level >= 7 && block.getType() == Material.EMERALD_ORE && plugin.hasChance(0.001)) {
            event.getBlock().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.EMERALD_BLOCK));
        }
    }
} 