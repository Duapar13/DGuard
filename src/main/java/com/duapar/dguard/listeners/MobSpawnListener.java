package com.duapar.dguard.listeners;

import com.duapar.dguard.manager.RegionManager;
import com.duapar.dguard.model.RegionFlag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.EnumSet;
import java.util.Set;

public class MobSpawnListener implements Listener {

    // On ne contrôle que le spawn "naturel" (monde ou spawner) ; les oeufs, les commandes
    // et le spawn déclenché par un plugin restent volontairement toujours autorisés.
    private static final Set<CreatureSpawnEvent.SpawnReason> CONTROLLED_REASONS = EnumSet.of(
            CreatureSpawnEvent.SpawnReason.NATURAL,
            CreatureSpawnEvent.SpawnReason.SPAWNER
    );

    private final RegionManager regionManager;

    public MobSpawnListener(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!CONTROLLED_REASONS.contains(event.getSpawnReason())) {
            return;
        }
        if (!regionManager.isAllowed(event.getLocation(), RegionFlag.MOB_SPAWN)) {
            event.setCancelled(true);
        }
    }
}
