package com.dguard.service;

import com.dapi.service.RegionService;
import com.dguard.manager.RegionManager;
import com.dguard.model.Region;
import com.dguard.model.RegionFlag;
import org.bukkit.Location;

/**
 * Implémentation de RegionService (contrat DAPI) adossée à RegionManager, pour
 * qu'un autre plugin D(nom) (ex: un futur DShop) puisse vérifier si une action
 * est autorisée à un endroit donné sans dépendre de DGuard.
 */
public class DGuardRegionServiceImpl implements RegionService {

    private final RegionManager regionManager;

    public DGuardRegionServiceImpl(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @Override
    public boolean isAllowed(Location location, String flag) {
        RegionFlag regionFlag = RegionFlag.fromInput(flag);
        if (regionFlag == null) {
            return true;
        }
        return regionManager.isAllowed(location, regionFlag);
    }

    @Override
    public String getRegionAt(Location location) {
        Region region = regionManager.getTopRegionAt(location);
        return region == null ? null : region.getName();
    }
}
