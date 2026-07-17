package com.duapar.dguard.storage;

import com.duapar.dguard.model.Region;

import java.util.Map;

public interface RegionStorage {

    void init() throws Exception;

    /**
     * Charge toutes les régions connues. Clé de la map = nom de région en minuscule.
     */
    Map<String, Region> loadRegions() throws Exception;

    void saveRegion(Region region);

    void deleteRegion(String nameLower);

    void close();
}
