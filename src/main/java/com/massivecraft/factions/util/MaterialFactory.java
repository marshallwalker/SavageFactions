package com.massivecraft.factions.util;

import org.bukkit.Material;

public class MaterialFactory {
    public static Material EXP_BOTTLE = findBestMatch("EXP_BOTTLE", "EXPERIENCE_BOTTLE");
    public static Material MOB_SPAWNER = findBestMatch("MOB_SPAWNER", "SPAWNER");
    public static Material LEGACY_CROPS = findBestMatch("CROPS");


    private static Material findBestMatch(String... names) {
        for (String name : names) {
            Material material = Material.matchMaterial(name);

            if (material != null) {
                return material;
            }
        }
        return Material.AIR;
    }
}