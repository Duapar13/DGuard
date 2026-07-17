package com.duapar.dguard.listeners;

import com.duapar.dguard.manager.RegionManager;
import com.duapar.dguard.model.RegionFlag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionListener implements Listener {

    private final RegionManager regionManager;

    public ExplosionListener(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> !regionManager.isAllowed(block.getLocation(), RegionFlag.EXPLOSION));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> !regionManager.isAllowed(block.getLocation(), RegionFlag.EXPLOSION));
    }
}
