package org.VodkaJpg.multitool.enchants;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class TelekineticEnchant extends Enchant implements Listener {
    public TelekineticEnchant() {
        super("Telekinetic", 1, Rarity.RARE, new ItemSet[]{ItemSet.PICKAXE, ItemSet.AXE, ItemSet.SHOVEL, ItemSet.HOE}, 
              "Automatycznie zbiera przedmioty do ekwipunku", 100, 0, true);
    }

    @Override
    public void playerBreakBlock(BlockBreakEvent e) {
        if (e.getPlayer().getInventory().firstEmpty() != -1) {
            e.setExpToDrop(0);
            for (ItemStack drop : e.getBlock().getDrops(e.getPlayer().getInventory().getItemInMainHand())) {
                e.getPlayer().getInventory().addItem(drop);
            }
        }
    }
}