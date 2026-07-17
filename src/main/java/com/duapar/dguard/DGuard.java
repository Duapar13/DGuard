package com.duapar.dguard;

import com.duapar.dguard.commands.RGCommand;
import com.duapar.dguard.integration.DAPIHook;
import com.duapar.dguard.listeners.EntryListener;
import com.duapar.dguard.listeners.ExplosionListener;
import com.duapar.dguard.listeners.FireListener;
import com.duapar.dguard.listeners.MobSpawnListener;
import com.duapar.dguard.listeners.ProtectionListener;
import com.duapar.dguard.listeners.PvpListener;
import com.duapar.dguard.manager.RegionManager;
import com.duapar.dguard.storage.MySQLRegionStorage;
import com.duapar.dguard.storage.RegionStorage;
import com.duapar.dguard.storage.YamlRegionStorage;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class DGuard extends JavaPlugin {

    private RegionStorage storage;
    private RegionManager regionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String storageType = getConfig().getString("storage.type", "local");
        if ("mysql".equalsIgnoreCase(storageType)) {
            storage = new MySQLRegionStorage(this,
                    getConfig().getString("storage.mysql.host", "localhost"),
                    getConfig().getInt("storage.mysql.port", 3306),
                    getConfig().getString("storage.mysql.database", "dguard"),
                    getConfig().getString("storage.mysql.username", "root"),
                    getConfig().getString("storage.mysql.password", ""),
                    getConfig().getBoolean("storage.mysql.useSSL", false));
        } else {
            storage = new YamlRegionStorage(getDataFolder(), getLogger());
        }

        try {
            storage.init();
        } catch (Exception e) {
            getLogger().severe("Impossible d'initialiser le stockage (" + storageType + "): " + e.getMessage());
            getLogger().severe("Le plugin va se désactiver.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        regionManager = new RegionManager(storage);
        try {
            regionManager.seed(storage.loadRegions());
        } catch (Exception e) {
            getLogger().severe("Erreur lors du chargement des régions existantes: " + e.getMessage());
        }

        RGCommand rgCommand = new RGCommand(this, regionManager);
        PluginCommand command = getCommand("region");
        if (command != null) {
            command.setExecutor(rgCommand);
            command.setTabCompleter(rgCommand);
        }

        getServer().getPluginManager().registerEvents(new ProtectionListener(regionManager), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(regionManager), this);
        getServer().getPluginManager().registerEvents(new MobSpawnListener(regionManager), this);
        getServer().getPluginManager().registerEvents(new PvpListener(regionManager), this);
        getServer().getPluginManager().registerEvents(new FireListener(regionManager), this);
        getServer().getPluginManager().registerEvents(new EntryListener(regionManager), this);

        DAPIHook.registerRegionService(this, regionManager);

        getLogger().info("DGuard activé (stockage: " + storageType + ").");
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.close();
        }
    }
}
