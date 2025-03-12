package org.VodkaJpg.multitool.listeners;

import org.VodkaJpg.multitool.Multitool;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class MultitoolListener implements Listener {
    private final Multitool plugin;

    public MultitoolListener(Multitool plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (!plugin.getItemUtils().isMultitool(tool)) return;

        Block block = event.getBlock();
        int level = plugin.getItemUtils().getMultitoolLevel(tool);

        // Zmiana typu narzędzia w zależności od bloku
        Material newToolType = getAppropriateToolType(block.getType());
        if (newToolType != null && newToolType != tool.getType()) {
            ItemStack newTool = tool.clone();
            newTool.setType(newToolType);
            event.getPlayer().getInventory().setItemInMainHand(newTool);
        }

        // Sprawdź bonusy
        if (level >= 3 && block.getType() == Material.IRON_ORE) {
            if (plugin.hasChance(plugin.getMultitoolConfig().getDouble("settings.iron-block-chance"))) {
                event.getPlayer().getInventory().addItem(new ItemStack(Material.IRON_BLOCK));
            }
        }

        if (level >= 7 && block.getType() == Material.GOLD_ORE) {
            if (plugin.hasChance(plugin.getMultitoolConfig().getDouble("settings.gold-block-chance"))) {
                event.getPlayer().getInventory().addItem(new ItemStack(Material.GOLD_BLOCK));
            }
        }

        // Auto-smelting dla poziomu 5
        if (level >= 5) {
            if (block.getType() == Material.IRON_ORE) {
                event.setExpToDrop(0);
                event.getPlayer().getInventory().addItem(new ItemStack(Material.IRON_INGOT));
            } else if (block.getType() == Material.GOLD_ORE) {
                event.setExpToDrop(0);
                event.getPlayer().getInventory().addItem(new ItemStack(Material.GOLD_INGOT));
            }
        }
    }

    private Material getAppropriateToolType(Material blockType) {
        // Dla bloków wymagających kilofa
        if (blockType.name().contains("ORE") || 
            blockType == Material.STONE || 
            blockType == Material.COBBLESTONE ||
            blockType == Material.DEEPSLATE ||
            blockType == Material.TUFF ||
            blockType == Material.CALCITE ||
            blockType == Material.AMETHYST_BLOCK ||
            blockType == Material.BUDDING_AMETHYST ||
            blockType == Material.AMETHYST_CLUSTER ||
            blockType == Material.LARGE_AMETHYST_BUD ||
            blockType == Material.MEDIUM_AMETHYST_BUD ||
            blockType == Material.SMALL_AMETHYST_BUD) {
            return Material.DIAMOND_PICKAXE;
        }
        
        // Dla bloków wymagających łopaty
        if (blockType.name().contains("DIRT") || 
            blockType == Material.SAND ||
            blockType == Material.GRAVEL ||
            blockType == Material.SOUL_SAND ||
            blockType == Material.SOUL_SOIL ||
            blockType == Material.CLAY ||
            blockType == Material.SNOW ||
            blockType == Material.SNOW_BLOCK ||
            blockType == Material.POWDER_SNOW ||
            blockType == Material.MUD ||
            blockType == Material.PACKED_MUD) {
            return Material.DIAMOND_SHOVEL;
        }
        
        // Dla bloków wymagających siekiery
        if (blockType.name().contains("LOG") || 
            blockType.name().contains("WOOD") ||
            blockType == Material.BAMBOO ||
            blockType == Material.CHERRY_LOG ||
            blockType == Material.CHERRY_WOOD ||
            blockType == Material.MANGROVE_LOG ||
            blockType == Material.MANGROVE_WOOD ||
            blockType == Material.MANGROVE_ROOTS) {
            return Material.DIAMOND_AXE;
        }
        
        // Dla bloków wymagających motyki
        if (blockType.name().contains("LEAVES") ||
            blockType == Material.WHEAT ||
            blockType == Material.CARROTS ||
            blockType == Material.POTATOES ||
            blockType == Material.BEETROOTS ||
            blockType == Material.MELON ||
            blockType == Material.PUMPKIN ||
            blockType == Material.NETHER_WART ||
            blockType == Material.SWEET_BERRY_BUSH ||
            blockType == Material.CAVE_VINES ||
            blockType == Material.WEEPING_VINES ||
            blockType == Material.TWISTING_VINES ||
            blockType == Material.KELP ||
            blockType == Material.SEAGRASS ||
            blockType == Material.MOSS_BLOCK ||
            blockType == Material.AZALEA ||
            blockType == Material.FLOWERING_AZALEA) {
            return Material.DIAMOND_HOE;
        }
        
        return null;
    }
} 