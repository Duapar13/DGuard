package com.duapar.dguard.listeners;

import com.duapar.dguard.manager.RegionManager;
import com.duapar.dguard.model.RegionFlag;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;

public class FireListener implements Listener {

    private final RegionManager regionManager;

    public FireListener(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent event) {
        if (!regionManager.isAllowed(event.getBlock().getLocation(), RegionFlag.FIRE_SPREAD)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event) {
        if (event.getSource().getType() != Material.FIRE) {
            return;
        }
        if (!regionManager.isAllowed(event.getBlock().getLocation(), RegionFlag.FIRE_SPREAD)) {
            event.setCancelled(true);
        }
    }
}
