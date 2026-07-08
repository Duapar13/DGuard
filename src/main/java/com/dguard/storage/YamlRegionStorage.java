package com.dguard.storage;

import com.dguard.model.Region;
import com.dguard.model.RegionFlag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YamlRegionStorage implements RegionStorage {

    private final File file;
    private final Logger logger;
    private YamlConfiguration config;

    public YamlRegionStorage(File dataFolder, Logger logger) {
        this.file = new File(new File(dataFolder, "data"), "regions.yml");
        this.logger = logger;
    }

    @Override
    public void init() throws IOException {
        File dir = file.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Impossible de créer le dossier de données " + dir);
        }
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Impossible de créer " + file);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public Map<String, Region> loadRegions() {
        Map<String, Region> result = new HashMap<>();
        ConfigurationSection root = config.getConfigurationSection("regions");
        if (root == null) {
            return result;
        }
        for (String nameLower : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(nameLower);
            if (section == null) continue;
            try {
                String displayName = section.getString("name", nameLower);
                String world = section.getString("world");
                int minX = section.getInt("minX");
                int minY = section.getInt("minY");
                int minZ = section.getInt("minZ");
                int maxX = section.getInt("maxX");
                int maxY = section.getInt("maxY");
                int maxZ = section.getInt("maxZ");
                int priority = section.getInt("priority", 0);
                String createdBy = section.getString("createdBy", "?");
                long createdAt = section.getLong("createdAt", System.currentTimeMillis());

                Region region = new Region(displayName, world, minX, minY, minZ, maxX, maxY, maxZ,
                        priority, createdBy, createdAt);

                ConfigurationSection flagsSection = section.getConfigurationSection("flags");
                if (flagsSection != null) {
                    for (String flagName : flagsSection.getKeys(false)) {
                        RegionFlag flag = RegionFlag.fromInput(flagName);
                        if (flag != null) {
                            region.setFlag(flag, flagsSection.getBoolean(flagName));
                        }
                    }
                }

                result.put(nameLower, region);
            } catch (RuntimeException ex) {
                logger.log(Level.WARNING, "Région invalide ignorée (nom=" + nameLower + "): " + ex.getMessage());
            }
        }
        return result;
    }

    @Override
    public synchronized void saveRegion(Region region) {
        String base = "regions." + region.getNameLower();
        config.set(base + ".name", region.getName());
        config.set(base + ".world", region.getWorld());
        config.set(base + ".minX", region.getMinX());
        config.set(base + ".minY", region.getMinY());
        config.set(base + ".minZ", region.getMinZ());
        config.set(base + ".maxX", region.getMaxX());
        config.set(base + ".maxY", region.getMaxY());
        config.set(base + ".maxZ", region.getMaxZ());
        config.set(base + ".priority", region.getPriority());
        config.set(base + ".createdBy", region.getCreatedBy());
        config.set(base + ".createdAt", region.getCreatedAt());

        config.set(base + ".flags", null);
        for (Map.Entry<RegionFlag, Boolean> entry : region.getFlags().entrySet()) {
            config.set(base + ".flags." + entry.getKey().getId(), entry.getValue());
        }

        save();
    }

    @Override
    public synchronized void deleteRegion(String nameLower) {
        config.set("regions." + nameLower, null);
        save();
    }

    @Override
    public void close() {
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Impossible de sauvegarder " + file, e);
        }
    }
}
