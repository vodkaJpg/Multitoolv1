package org.VodkaJpg.multitool.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerDamaged extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final double damage;
    private final boolean cancelled;

    public PlayerDamaged(Player player, double damage, boolean cancelled) {
        this.player = player;
        this.damage = damage;
        this.cancelled = cancelled;
    }

    public Player getPlayer() {
        return player;
    }

    public double getDamage() {
        return damage;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
} 