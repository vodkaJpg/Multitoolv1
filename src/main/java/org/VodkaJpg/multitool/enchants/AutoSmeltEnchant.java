package org.VodkaJpg.multitool.enchants;

import org.VodkaJpg.multitool.Multitool;
import org.VodkaJpg.multitool.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;


import java.util.HashMap;
import java.util.Map;

public class AutoSmeltEnchant extends Enchant {
    private final Map<Material, Material> smeltMap;

    public AutoSmeltEnchant() {
        super("Auto-Smelt", 1, Rarity.RARE, new ItemSet[]{ItemSet.TOOL}, "Automatycznie przetapia wykopane rudy", 100, 0);
        this.smeltMap = new HashMap<>();
        initializeSmeltMap();
    }

    private void initializeSmeltMap() {
        smeltMap.put(Material.IRON_ORE, Material.IRON_INGOT);
        smeltMap.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        smeltMap.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        smeltMap.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        smeltMap.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        smeltMap.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        smeltMap.put(Material.RAW_IRON, Material.IRON_INGOT);
        smeltMap.put(Material.RAW_GOLD, Material.GOLD_INGOT);
        smeltMap.put(Material.RAW_COPPER, Material.COPPER_INGOT);
    }

  

    @Override
    public void playerBreakBlock(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Block block = event.getBlock();
        Material blockType = block.getType();
        
        if (!smeltMap.containsKey(blockType)) return;
        
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (!ItemUtils.hasEnchant(tool, this)) return;
        
        event.setExpToDrop(0);
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smeltMap.get(blockType)));
    }
}