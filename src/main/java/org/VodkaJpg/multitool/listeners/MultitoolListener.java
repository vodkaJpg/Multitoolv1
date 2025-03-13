package org.VodkaJpg.multitool.listeners;

import org.VodkaJpg.multitool.Multitool;
import org.VodkaJpg.multitool.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
    private final Map<String, Long> blocksMined = new HashMap<>();

    public MultitoolListener(Multitool plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (!plugin.getItemUtils().isMultitool(tool)) return;

        // Zmień typ narzędzia w zależności od bloku
        changeToolType(tool, event.getBlock());

        // Zwiększ licznik wykopanych bloków
        String playerName = event.getPlayer().getName();
        long blocks = blocksMined.getOrDefault(playerName, 0L) + 1;
        blocksMined.put(playerName, blocks);

        // Aktualizuj opis przedmiotu
        int level = plugin.getItemUtils().getMultitoolLevel(tool);
        ItemStack updatedTool = plugin.getItemUtils().createMultitool(level, blocks);
        event.getPlayer().getInventory().setItemInMainHand(updatedTool);

        // Sprawdź czy gracz może awansować na następny poziom
        checkLevelUp(event.getPlayer(), updatedTool);

        // Zastosuj bonusy w zależności od poziomu
        applyBonuses(event);
    }

    private void checkLevelUp(org.bukkit.entity.Player player, ItemStack tool) {
        int currentLevel = plugin.getItemUtils().getMultitoolLevel(tool);
        if (currentLevel >= 7) return; // Maksymalny poziom

        String playerName = player.getName();
        long blocks = blocksMined.getOrDefault(playerName, 0L);
        
        // Pobierz wymagane bloki dla następnego poziomu
        ConfigurationSection levelConfig = plugin.getMultitoolConfig().getConfigurationSection("levels." + currentLevel);
        if (levelConfig == null) return;
        
        long requiredBlocks = levelConfig.getLong("required_blocks", 0);
        
        // Sprawdź czy gracz może awansować
        if (blocks >= requiredBlocks) {
            // Zresetuj licznik bloków
            blocksMined.put(playerName, 0L);
            
            // Zwiększ poziom
            ItemStack newTool = plugin.getItemUtils().createMultitool(currentLevel + 1, 0);
            player.getInventory().setItemInMainHand(newTool);
            
            // Wyślij wiadomość o awansie
            Map<String, String> replacements = new HashMap<>();
            replacements.put("level", String.valueOf(currentLevel + 1));
            player.sendMessage(plugin.getMessageManager().getSuccess("level_up", replacements));
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
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
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
                event.setExpToDrop(0);
                event.getBlock().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.IRON_INGOT));
            } else if (block.getType() == Material.GOLD_ORE) {
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