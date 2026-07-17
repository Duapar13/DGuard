package com.duapar.dguard.model;

import org.bukkit.Location;

import java.util.EnumMap;
import java.util.Map;

public class Region {

    private String name;
    private final String world;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;
    private int priority;
    private final String createdBy;
    private final long createdAt;
    private final Map<RegionFlag, Boolean> flags = new EnumMap<>(RegionFlag.class);

    public Region(String name, String world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                  int priority, String createdBy, long createdAt) {
        this.name = name;
        this.world = world;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.priority = priority;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public String getNameLower() {
        return name.toLowerCase();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorld() {
        return world;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getVolume() {
        return (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }

    public Map<RegionFlag, Boolean> getFlags() {
        return flags;
    }

    public void setFlag(RegionFlag flag, boolean allow) {
        flags.put(flag, allow);
    }

    /**
     * @return {@code true} (allow) si le flag n'a jamais été explicitement configuré -
     * une région fraîchement créée est donc entièrement protégée par défaut.
     */
    public boolean isAllowed(RegionFlag flag) {
        return flags.getOrDefault(flag, false);
    }

    public boolean contains(Location location) {
        if (location.getWorld() == null || !location.getWorld().getName().equals(world)) {
            return false;
        }
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
}
