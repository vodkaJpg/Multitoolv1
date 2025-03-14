package org.VodkaJpg.multitool.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerAttackedEntity extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Entity target;
    private final double damage;
    private final boolean cancelled;

    public PlayerAttackedEntity(Player player, Entity target, double damage, boolean cancelled) {
        this.player = player;
        this.target = target;
        this.damage = damage;
        this.cancelled = cancelled;
    }

    public Player getPlayer() {
        return player;
    }

    public Entity getTarget() {
        return target;
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