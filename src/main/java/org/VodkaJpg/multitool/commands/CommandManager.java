package org.VodkaJpg.multitool.commands;

import org.VodkaJpg.multitool.Multitool;
import org.VodkaJpg.multitool.enchants.TelekineticEnchant;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final Multitool plugin;

    public CommandManager(Multitool plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getError("player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                handleGiveCommand(player, args);
                break;
            case "prestige":
                handlePrestigeCommand(player);
                break;
            case "reload":
                if (player.hasPermission("multitool.admin")) {
                    plugin.reloadConfigs();
                    player.sendMessage(plugin.getMessageManager().getSuccess("reload"));
                } else {
                    player.sendMessage(plugin.getMessageManager().getError("no_permission"));
                }
                break;
            case "enchant":
                handleEnchantCommand(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void handleGiveCommand(Player player, String[] args) {
        if (!player.hasPermission("multitool.admin")) {
            player.sendMessage(plugin.getMessageManager().getError("no_permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getCommandMessage("give"));
            return;
        }

        try {
            int level = Integer.parseInt(args[1]);
            if (level < 1 || level > 7) {
                player.sendMessage(plugin.getMessageManager().getError("invalid_level"));
                return;
            }

            Player target = player;
            if (args.length > 2) {
                target = plugin.getServer().getPlayer(args[2]);
                if (target == null) {
                    player.sendMessage(plugin.getMessageManager().getError("player_not_found"));
                    return;
                }
            }

            target.getInventory().addItem(plugin.getItemUtils().createMultitool(level));
            
            Map<String, String> replacements = new HashMap<>();
            replacements.put("player", target.getName());
            replacements.put("level", String.valueOf(level));
            player.sendMessage(plugin.getMessageManager().getSuccess("give", replacements));
            
            if (target != player) {
                target.sendMessage(plugin.getMessageManager().getSuccess("received", replacements));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getMessageManager().getError("invalid_level"));
        }
    }

    private void handlePrestigeCommand(Player player) {
        if (!player.hasPermission("multitool.prestige")) {
            player.sendMessage(plugin.getMessageManager().getError("no_permission"));
            return;
        }

        if (!plugin.getItemUtils().isMultitool(player.getInventory().getItemInMainHand())) {
            player.sendMessage(plugin.getMessageManager().getError("no_multitool"));
            return;
        }

        int currentLevel = plugin.getItemUtils().getMultitoolLevel(player.getInventory().getItemInMainHand());
        if (currentLevel < 7) {
            player.sendMessage(plugin.getMessageManager().getError("max_level"));
            return;
        }

        player.getInventory().setItemInMainHand(plugin.getItemUtils().createMultitool(8)); // 8 to poziom prestiżowy
        Map<String, String> replacements = new HashMap<>();
        replacements.put("level", "Prestige");
        player.sendMessage(plugin.getMessageManager().getSuccess("prestige", replacements));
    }

    private void handleEnchantCommand(Player player, String[] args) {
        if (!player.hasPermission("multitool.admin")) {
            player.sendMessage(plugin.getMessageManager().getError("no_permission"));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(plugin.getMessageManager().getCommandMessage("enchant"));
            return;
        }

        String enchantName = args[1].toLowerCase();
        int level;
        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getMessageManager().getError("invalid_level"));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(plugin.getMessageManager().getError("no_item"));
            return;
        }

        switch (enchantName) {
            case "telekinetic":
                if (level > plugin.getTelekineticEnchant().getMaxLevel()) {
                    player.sendMessage(plugin.getMessageManager().getError("invalid_level"));
                    return;
                }
                plugin.getItemUtils().addCustomEnchant(item, plugin.getTelekineticEnchant(), level);
                player.sendMessage(plugin.getMessageManager().getSuccess("enchant_added", Map.of(
                    "enchant", "Telekinetic",
                    "level", String.valueOf(level)
                )));
                break;
            default:
                player.sendMessage(plugin.getMessageManager().getError("invalid_enchant"));
                break;
        }
    }

    private void sendHelp(Player player) {
        String helpMessage = plugin.getMessageManager().getCommandMessage("help");
        for (String line : helpMessage.split("\n")) {
            player.sendMessage(line);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("multitool.admin")) {
                completions.addAll(Arrays.asList("give", "reload", "enchant"));
            }
            if (sender.hasPermission("multitool.prestige")) {
                completions.add("prestige");
            }
            return completions;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give") && sender.hasPermission("multitool.admin")) {
                for (int i = 1; i <= 7; i++) {
                    completions.add(String.valueOf(i));
                }
            } else if (args[0].equalsIgnoreCase("enchant") && sender.hasPermission("multitool.admin")) {
                completions.add("telekinetic");
            }
            return completions;
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give") && sender.hasPermission("multitool.admin")) {
                plugin.getServer().getOnlinePlayers().forEach(player -> completions.add(player.getName()));
            } else if (args[0].equalsIgnoreCase("enchant") && args[1].equalsIgnoreCase("telekinetic")) {
                completions.add("1");
            }
            return completions;
        }

        return completions;
    }
} 