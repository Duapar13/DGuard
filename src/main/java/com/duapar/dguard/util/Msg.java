package com.duapar.dguard.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class Msg {

    public static final String PREFIX =
            ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "DGuard" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;

    private Msg() {
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + message);
    }

    public static void success(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.GREEN + message);
    }

    public static void error(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.RED + message);
    }

    public static void warn(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "⚠ " + message);
    }
}
