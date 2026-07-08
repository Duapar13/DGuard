package com.dguard.integration;

import com.dapi.DAPI;
import com.dapi.service.FactionService;
import com.dapi.service.RegionService;
import com.dapi.service.SelectionService;
import com.dguard.manager.RegionManager;
import com.dguard.service.DGuardRegionServiceImpl;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Centralise les appels à DAPI. DGuard dépend "en dur" de DAPI (depend: [DAPI] dans
 * plugin.yml) : contrairement à DWorldEdit/DTicket, aucune isolation contre
 * NoClassDefFoundError n'est nécessaire ici, DAPI est garanti présent au démarrage.
 * FactionService et SelectionService restent individuellement optionnels (DFaction/
 * DWorldEdit peuvent chacun être absents), d'où les vérifications de nullité.
 */
public final class DAPIHook {

    private DAPIHook() {
    }

    public static void registerRegionService(JavaPlugin plugin, RegionManager regionManager) {
        DAPI.registerPlugin(plugin, "RegionService");
        DAPI.registerService(RegionService.class, new DGuardRegionServiceImpl(regionManager), plugin);
    }

    public static Location getSelectionPos1(UUID playerId) {
        SelectionService service = DAPI.getService(SelectionService.class);
        return service == null ? null : service.getPos1(playerId);
    }

    public static Location getSelectionPos2(UUID playerId) {
        SelectionService service = DAPI.getService(SelectionService.class);
        return service == null ? null : service.getPos2(playerId);
    }

    public static boolean isSelectionServiceAvailable() {
        return DAPI.getService(SelectionService.class) != null;
    }

    /**
     * @return les noms des factions dont le territoire est touché par la zone
     * [minX..maxX] x [minZ..maxZ] (granularité chunk), ou une liste vide si
     * FactionService n'est pas disponible (DFaction non installé).
     */
    public static List<String> findClaimedFactions(World world, int minX, int minZ, int maxX, int maxZ) {
        FactionService factionService = DAPI.getService(FactionService.class);
        if (factionService == null) {
            return Collections.emptyList();
        }
        Set<String> found = new LinkedHashSet<>();
        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                Location probe = new Location(world, cx * 16.0, 64, cz * 16.0);
                String owner = factionService.getClaimOwner(probe);
                if (owner != null) {
                    found.add(owner);
                }
            }
        }
        return new ArrayList<>(found);
    }
}
