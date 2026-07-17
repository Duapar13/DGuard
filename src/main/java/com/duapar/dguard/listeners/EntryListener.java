package com.duapar.dguard.listeners;

import com.duapar.dguard.manager.RegionManager;
import com.duapar.dguard.model.Region;
import com.duapar.dguard.model.RegionFlag;
import com.duapar.dguard.util.Msg;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntryListener implements Listener {

    private static final long DENY_MESSAGE_COOLDOWN_MS = 2000L;

    private final RegionManager regionManager;
    private final Map<UUID, Long> lastDenyMessage = new HashMap<>();

    public EntryListener(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || !changedBlock(from, to)) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission("dguard.admin")) {
            return;
        }

        if (!regionManager.isAllowed(to, RegionFlag.ENTRY)) {
            event.setCancelled(true);
            denyMessage(player, to);
        }
    }

    private boolean changedBlock(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ();
    }

    private void denyMessage(Player player, Location location) {
        long now = System.currentTimeMillis();
        Long last = lastDenyMessage.get(player.getUniqueId());
        if (last != null && now - last < DENY_MESSAGE_COOLDOWN_MS) {
            return;
        }
        lastDenyMessage.put(player.getUniqueId(), now);

        Region region = regionManager.getTopRegionAt(location);
        String name = region != null ? region.getName() : "?";
        Msg.error(player, "Entrée interdite dans la région " + name + ".");
    }
}
