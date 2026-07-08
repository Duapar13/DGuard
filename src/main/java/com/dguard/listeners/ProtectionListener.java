package com.dguard.listeners;

import com.dguard.manager.RegionManager;
import com.dguard.model.Region;
import com.dguard.model.RegionFlag;
import com.dguard.util.Msg;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ProtectionListener implements Listener {

    private static final Set<Material> PROTECTED_WORKSTATIONS = EnumSet.of(
            Material.CRAFTING_TABLE,
            Material.ANVIL,
            Material.CHIPPED_ANVIL,
            Material.DAMAGED_ANVIL,
            Material.ENCHANTING_TABLE,
            Material.GRINDSTONE,
            Material.CARTOGRAPHY_TABLE,
            Material.LOOM,
            Material.STONECUTTER,
            Material.SMITHING_TABLE,
            Material.BEACON,
            Material.LECTERN,
            Material.JUKEBOX,
            Material.ENDER_CHEST
    );

    private static final long DENY_MESSAGE_COOLDOWN_MS = 2000L;

    private final RegionManager regionManager;
    private final Map<UUID, Long> lastDenyMessage = new HashMap<>();

    public ProtectionListener(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (bypasses(player)) {
            return;
        }
        Location location = event.getBlock().getLocation();
        if (!regionManager.isAllowed(location, RegionFlag.BREAK)) {
            event.setCancelled(true);
            denyMessage(player, location);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (bypasses(player)) {
            return;
        }
        Location location = event.getBlock().getLocation();
        if (!regionManager.isAllowed(location, RegionFlag.PLACE)) {
            event.setCancelled(true);
            denyMessage(player, location);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || !isProtectedInteractable(block)) {
            return;
        }
        Player player = event.getPlayer();
        if (bypasses(player)) {
            return;
        }
        Location location = block.getLocation();
        if (!regionManager.isAllowed(location, RegionFlag.INTERACT)) {
            event.setCancelled(true);
            denyMessage(player, location);
        }
    }

    private boolean isProtectedInteractable(Block block) {
        if (PROTECTED_WORKSTATIONS.contains(block.getType())) {
            return true;
        }
        return block.getState() instanceof InventoryHolder;
    }

    private boolean bypasses(Player player) {
        return player.hasPermission("dguard.admin");
    }

    private void denyMessage(Player player, Location location) {
        long now = System.currentTimeMillis();
        Long last = lastDenyMessage.get(player.getUniqueId());
        if (last != null && now - last < DENY_MESSAGE_COOLDOWN_MS) {
            return;
        }
        lastDenyMessage.put(player.getUniqueId(), now);

        Region region = regionManager.getTopRegionAt(location);
        String suffix = region != null ? " (région " + region.getName() + ")" : "";
        Msg.error(player, "Action interdite ici" + suffix + ".");
    }
}
