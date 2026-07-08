package com.dguard.model;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Les flags configurables d'une région, façon WorldGuard. Chaque flag accepte
 * un ou plusieurs alias en entrée de commande (ex: "griefing" pour BREAK).
 */
public enum RegionFlag {

    BREAK("break", "griefing"),
    PLACE("place", "build"),
    EXPLOSION("explosion", "tnt"),
    MOB_SPAWN("mob-spawn", "mobspawn", "mob"),
    PVP("pvp"),
    INTERACT("interact", "use"),
    FIRE_SPREAD("fire-spread", "fire"),
    ENTRY("entry");

    private final List<String> aliases;

    RegionFlag(String... aliases) {
        this.aliases = Arrays.asList(aliases);
    }

    public String getId() {
        return aliases.get(0);
    }

    public static RegionFlag fromInput(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        for (RegionFlag flag : values()) {
            if (flag.aliases.contains(lower)) {
                return flag;
            }
        }
        return null;
    }
}
