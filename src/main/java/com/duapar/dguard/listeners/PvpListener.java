package com.duapar.dguard.listeners;

import com.duapar.dguard.manager.RegionManager;
import com.duapar.dguard.model.RegionFlag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpListener implements Listener {

    private final RegionManager regionManager;

    public PvpListener(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player victim = (Player) event.getEntity();
        if (resolveAttacker(event.getDamager()) == null) {
            return;
        }
        if (!regionManager.isAllowed(victim.getLocation(), RegionFlag.PVP)) {
            event.setCancelled(true);
        }
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        }
        if (damager instanceof Projectile) {
            Object shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
        }
        return null;
    }
}
