package com.duapar.dguard.manager;

import com.duapar.dguard.model.Region;
import com.duapar.dguard.model.RegionFlag;
import com.duapar.dguard.storage.RegionStorage;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionManager {

    private final RegionStorage storage;
    private final Map<String, Region> regions = new HashMap<>();

    public RegionManager(RegionStorage storage) {
        this.storage = storage;
    }

    public void seed(Map<String, Region> loaded) {
        regions.clear();
        regions.putAll(loaded);
    }

    public Region create(String rawName, Location pos1, Location pos2, String createdBy) {
        if (rawName == null || rawName.isEmpty()) {
            throw new RegionException("Utilisation: /rg create <nom>");
        }
        if (rawName.length() > 32) {
            throw new RegionException("Le nom d'une région ne peut pas dépasser 32 caractères.");
        }
        if (!rawName.matches("^[A-Za-z0-9_]+$")) {
            throw new RegionException("Le nom d'une région ne peut contenir que des lettres, chiffres et _.");
        }
        String lower = rawName.toLowerCase();
        if (regions.containsKey(lower)) {
            throw new RegionException("Une région avec ce nom existe déjà.");
        }
        if (pos1.getWorld() == null || !pos1.getWorld().equals(pos2.getWorld())) {
            throw new RegionException("La sélection doit être dans un seul et même monde.");
        }

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        Region region = new Region(rawName, pos1.getWorld().getName(), minX, minY, minZ, maxX, maxY, maxZ,
                0, createdBy, System.currentTimeMillis());
        regions.put(lower, region);
        storage.saveRegion(region);
        return region;
    }

    public void delete(String name) {
        Region region = getOrThrow(name);
        regions.remove(region.getNameLower());
        storage.deleteRegion(region.getNameLower());
    }

    public Region getOrThrow(String name) {
        Region region = regions.get(name.toLowerCase());
        if (region == null) {
            throw new RegionException("Région introuvable: " + name);
        }
        return region;
    }

    public List<Region> getAll() {
        List<Region> result = new ArrayList<>(regions.values());
        result.sort(Comparator.comparing(Region::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public void setFlag(String name, RegionFlag flag, boolean allow) {
        Region region = getOrThrow(name);
        region.setFlag(flag, allow);
        storage.saveRegion(region);
    }

    public void setPriority(String name, int priority) {
        Region region = getOrThrow(name);
        region.setPriority(priority);
        storage.saveRegion(region);
    }

    /**
     * Régions couvrant cet endroit, triées par priorité décroissante (la plus haute
     * priorité en premier ; à égalité, la plus récemment créée gagne).
     */
    public List<Region> getRegionsAt(Location location) {
        List<Region> found = new ArrayList<>();
        for (Region region : regions.values()) {
            if (region.contains(location)) {
                found.add(region);
            }
        }
        found.sort(Comparator.comparingInt(Region::getPriority).reversed()
                .thenComparing(Comparator.comparingLong(Region::getCreatedAt).reversed()));
        return found;
    }

    public Region getTopRegionAt(Location location) {
        List<Region> found = getRegionsAt(location);
        return found.isEmpty() ? null : found.get(0);
    }

    /**
     * @return {@code true} si aucune région ne couvre cet endroit, ou si la région de
     * plus haute priorité qui le couvre autorise ce flag.
     */
    public boolean isAllowed(Location location, RegionFlag flag) {
        Region top = getTopRegionAt(location);
        return top == null || top.isAllowed(flag);
    }
}
