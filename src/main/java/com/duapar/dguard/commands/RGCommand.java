package com.duapar.dguard.commands;

import com.duapar.dguard.integration.DAPIHook;
import com.duapar.dguard.manager.RegionException;
import com.duapar.dguard.manager.RegionManager;
import com.duapar.dguard.model.Region;
import com.duapar.dguard.model.RegionFlag;
import com.duapar.dguard.util.Msg;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RGCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "create", "delete", "list", "info", "perm", "priority", "help"
    );
    private static final List<String> NAME_ARG_SUBCOMMANDS = Arrays.asList("delete", "info", "perm", "priority");

    private final JavaPlugin plugin;
    private final RegionManager regionManager;

    public RGCommand(JavaPlugin plugin, RegionManager regionManager) {
        this.plugin = plugin;
        this.regionManager = regionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("dguard.admin")) {
            Msg.error(sender, "Tu n'as pas la permission de gérer les régions.");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        try {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "create":
                    handleCreate(sender, args);
                    break;
                case "delete":
                    handleDelete(sender, args);
                    break;
                case "list":
                    handleList(sender);
                    break;
                case "info":
                    handleInfo(sender, args);
                    break;
                case "perm":
                    handlePerm(sender, args);
                    break;
                case "priority":
                    handlePriority(sender, args);
                    break;
                case "help":
                    sendHelp(sender);
                    break;
                default:
                    sendHelp(sender);
                    break;
            }
        } catch (RegionException e) {
            Msg.error(sender, e.getMessage());
        }
        return true;
    }

    private Player requirePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            throw new RegionException("Seul un joueur peut utiliser cette commande.");
        }
        return (Player) sender;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 2) {
            throw new RegionException("Utilisation: /rg create <nom>");
        }
        Player player = requirePlayer(sender);

        if (!DAPIHook.isSelectionServiceAvailable()) {
            throw new RegionException("Aucun plugin de sélection (ex: DWorldEdit) n'est installé.");
        }
        Location pos1 = DAPIHook.getSelectionPos1(player.getUniqueId());
        Location pos2 = DAPIHook.getSelectionPos2(player.getUniqueId());
        if (pos1 == null || pos2 == null) {
            throw new RegionException("Sélectionne d'abord une zone (//wand puis clics gauche/droit avec DWorldEdit).");
        }

        Region region = regionManager.create(args[1], pos1, pos2, player.getName());
        Msg.success(sender, "Région " + ChatColor.GOLD + region.getName() + ChatColor.GREEN + " créée ("
                + region.getVolume() + " blocs). Tout est refusé par défaut, configure avec /rg perm.");

        if (plugin.getConfig().getBoolean("warn-on-faction-claim", true)) {
            List<String> factions = DAPIHook.findClaimedFactions(pos1.getWorld(),
                    region.getMinX(), region.getMinZ(), region.getMaxX(), region.getMaxZ());
            if (!factions.isEmpty()) {
                Msg.warn(sender, "Cette région chevauche le territoire de la faction " + String.join(", ", factions) + ".");
            }
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            throw new RegionException("Utilisation: /rg delete <nom>");
        }
        Region region = regionManager.getOrThrow(args[1]);
        regionManager.delete(args[1]);
        Msg.success(sender, "Région " + region.getName() + " supprimée.");
    }

    private void handleList(CommandSender sender) {
        List<Region> regions = regionManager.getAll();
        if (regions.isEmpty()) {
            Msg.send(sender, "Aucune région créée pour le moment.");
            return;
        }
        sender.sendMessage(ChatColor.DARK_GRAY + "==== " + ChatColor.DARK_AQUA + "Régions (" + regions.size() + ")" + ChatColor.DARK_GRAY + " ====");
        for (Region region : regions) {
            sender.sendMessage(ChatColor.GOLD + region.getName() + ChatColor.GRAY + " - " + region.getWorld()
                    + " - priorité " + region.getPriority() + " - " + region.getVolume() + " blocs");
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            throw new RegionException("Utilisation: /rg info <nom>");
        }
        Region region = regionManager.getOrThrow(args[1]);

        sender.sendMessage(ChatColor.DARK_GRAY + "==== " + ChatColor.DARK_AQUA + region.getName() + ChatColor.DARK_GRAY + " ====");
        sender.sendMessage(ChatColor.GRAY + "Monde: " + ChatColor.WHITE + region.getWorld());
        sender.sendMessage(ChatColor.GRAY + "Bornes: " + ChatColor.WHITE
                + "(" + region.getMinX() + ", " + region.getMinY() + ", " + region.getMinZ() + ") -> ("
                + region.getMaxX() + ", " + region.getMaxY() + ", " + region.getMaxZ() + ")");
        sender.sendMessage(ChatColor.GRAY + "Volume: " + ChatColor.WHITE + region.getVolume() + " blocs");
        sender.sendMessage(ChatColor.GRAY + "Priorité: " + ChatColor.WHITE + region.getPriority());
        sender.sendMessage(ChatColor.GRAY + "Créée par: " + ChatColor.WHITE + region.getCreatedBy());

        StringBuilder flags = new StringBuilder();
        for (RegionFlag flag : RegionFlag.values()) {
            boolean allowed = region.isAllowed(flag);
            flags.append(allowed ? ChatColor.GREEN : ChatColor.RED).append(flag.getId()).append(ChatColor.GRAY).append(' ');
        }
        sender.sendMessage(ChatColor.GRAY + "Flags: " + flags);
    }

    private void handlePerm(CommandSender sender, String[] args) {
        if (args.length < 4) {
            throw new RegionException("Utilisation: /rg perm <nom> <flag> <allow|deny>");
        }
        Region region = regionManager.getOrThrow(args[1]);
        RegionFlag flag = RegionFlag.fromInput(args[2]);
        if (flag == null) {
            throw new RegionException("Flag inconnu: " + args[2]
                    + ". Utilise: break, place, explosion, mob-spawn, pvp, interact, fire-spread, entry.");
        }
        boolean allow = parseAllowDeny(args[3]);
        regionManager.setFlag(region.getName(), flag, allow);
        Msg.success(sender, "Flag " + flag.getId() + " de la région " + region.getName() + " -> " + (allow ? "allow" : "deny") + ".");
    }

    private boolean parseAllowDeny(String raw) {
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.equals("allow") || lower.equals("true") || lower.equals("yes")) {
            return true;
        }
        if (lower.equals("deny") || lower.equals("false") || lower.equals("no")) {
            return false;
        }
        throw new RegionException("Valeur invalide: " + raw + " (attendu: allow ou deny).");
    }

    private void handlePriority(CommandSender sender, String[] args) {
        if (args.length < 3) {
            throw new RegionException("Utilisation: /rg priority <nom> <valeur>");
        }
        Region region = regionManager.getOrThrow(args[1]);
        int priority;
        try {
            priority = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            throw new RegionException("Valeur de priorité invalide: " + args[2]);
        }
        regionManager.setPriority(region.getName(), priority);
        Msg.success(sender, "Priorité de la région " + region.getName() + " -> " + priority + ".");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_GRAY + "==== " + ChatColor.DARK_AQUA + "DGuard" + ChatColor.DARK_GRAY + " ====");
        sender.sendMessage(ChatColor.GOLD + "/rg create <nom>" + ChatColor.GRAY + " - Créer une région à partir de ta sélection DWorldEdit.");
        sender.sendMessage(ChatColor.GOLD + "/rg delete <nom>" + ChatColor.GRAY + " - Supprimer une région.");
        sender.sendMessage(ChatColor.GOLD + "/rg list" + ChatColor.GRAY + " - Lister toutes les régions.");
        sender.sendMessage(ChatColor.GOLD + "/rg info <nom>" + ChatColor.GRAY + " - Voir le détail d'une région.");
        sender.sendMessage(ChatColor.GOLD + "/rg perm <nom> <flag> <allow|deny>" + ChatColor.GRAY + " - Configurer un flag.");
        sender.sendMessage(ChatColor.GOLD + "/rg priority <nom> <valeur>" + ChatColor.GRAY + " - Priorité en cas de chevauchement (plus haut gagne).");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);
            return SUBCOMMANDS.stream().filter(s -> s.startsWith(partial)).collect(Collectors.toList());
        }
        if (args.length == 2 && NAME_ARG_SUBCOMMANDS.contains(args[0].toLowerCase(Locale.ROOT))) {
            String partial = args[1].toLowerCase(Locale.ROOT);
            return regionManager.getAll().stream()
                    .map(Region::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(partial))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("perm")) {
            String partial = args[2].toLowerCase(Locale.ROOT);
            return Arrays.stream(RegionFlag.values())
                    .map(RegionFlag::getId)
                    .filter(id -> id.startsWith(partial))
                    .collect(Collectors.toList());
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("perm")) {
            String partial = args[3].toLowerCase(Locale.ROOT);
            return Arrays.asList("allow", "deny").stream().filter(s -> s.startsWith(partial)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
