package org.VodkaJpg.multitool.commands;

import org.VodkaJpg.multitool.Multitool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                    plugin.reloadConfig();
                    plugin.getMessageManager().loadMessages();
                    player.sendMessage(plugin.getMessageManager().getSuccess("reload"));
                } else {
                    player.sendMessage(plugin.getMessageManager().getError("no_permission"));
                }
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
            player.sendMessage(plugin.getMessageManager().getError("give_usage"));
            return;
        }

        try {
            int level = Integer.parseInt(args[1]);
            if (level < 1 || level > 7) {
                player.sendMessage(plugin.getMessageManager().getError("level_range"));
                return;
            }
            player.getInventory().addItem(plugin.getItemUtils().createMultitool(level));
            player.sendMessage(plugin.getMessageManager().getSuccess("give").replace("{level}", String.valueOf(level)));
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getMessageManager().getError("invalid_level"));
        }
    }

    private void handlePrestigeCommand(Player player) {
        if (!player.hasPermission("multitool.prestige")) {
            player.sendMessage(plugin.getMessageManager().getError("no_permission"));
            return;
        }

        if (plugin.getItemUtils().isMultitool(player.getInventory().getItemInMainHand())) {
            player.getInventory().setItemInMainHand(plugin.getItemUtils().createMultitool(8)); // 8 to poziom prestiżowy
            player.sendMessage(plugin.getMessageManager().getSuccess("prestige"));
        } else {
            player.sendMessage(plugin.getMessageManager().getError("hold_multitool"));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(plugin.getMessageManager().getCommandMessage("help.title"));
        if (player.hasPermission("multitool.admin")) {
            player.sendMessage(plugin.getMessageManager().getCommandMessage("help.give"));
            player.sendMessage(plugin.getMessageManager().getCommandMessage("help.reload"));
        }
        if (player.hasPermission("multitool.prestige")) {
            player.sendMessage(plugin.getMessageManager().getCommandMessage("help.prestige"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("give", "prestige"));
            if (sender.hasPermission("multitool.admin")) {
                completions.add("reload");
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("multitool.admin")) {
            for (int i = 1; i <= 7; i++) {
                completions.add(String.valueOf(i));
            }
            return completions;
        }

        return completions;
    }
} 