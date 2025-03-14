package org.VodkaJpg.multitool.enchants;

import org.VodkaJpg.multitool.Multitool;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.NamespacedKey;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;

public class AutoSmeltEnchant extends Enchant implements Listener {
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
    public String getName() {
        return "Auto-Smelt";
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return false;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item.getType().name().endsWith("_PICKAXE") || 
               item.getType().name().endsWith("_AXE") || 
               item.getType().name().endsWith("_SHOVEL");
    }

    @Override
    public String getTranslationKey() {
        return "enchantment.auto_smelt";
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public String translationKey() {
        return "enchantment.auto_smelt";
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public int getWeight() {
        return 10;
    }

    @Override
    public int getMaxModifiedCost(int level) {
        return 50;
    }

    @Override
    public NamespacedKey getKey() {
        return new NamespacedKey(Multitool.getInstance(), "auto_smelt");
    }

    @Override
    public int getMinModifiedCost(int level) {
        return 10;
    }

    @Override
    public Component displayName(int level) {
        return Component.text("Auto-Smelt " + (level > 1 ? level : ""));
    }

    @Override
    public Component description() {
        return Component.text("Automatycznie przetapia wykopane rudy");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
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