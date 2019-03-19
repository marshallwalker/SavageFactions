package com.massivecraft.factions;

import com.massivecraft.factions.util.Particles.ReflectionUtils;

public enum ServerVersion {

    MC_V17,
    MC_V18,
    MC_V1_13,
    UNKNOWN;

    public static ServerVersion getVersion() {
        int versionCode = Integer.parseInt(ReflectionUtils.PackageType.getServerVersion().split("_")[1]);

        if (versionCode == 7) {
            SavageFactionsPlugin.plugin.log("Minecraft Version 1.7 found, disabling banners, itemflags inside GUIs, and Titles.");
            return MC_V17;
        }

        if (versionCode == 8) {
            SavageFactionsPlugin.plugin.log("Minecraft Version 1.8 found, Title Fadeouttime etc will not be configurable.");
            return MC_V18;
        }

        if (versionCode == 13) {
            SavageFactionsPlugin.plugin.log("Minecraft Version 1.13 found, New Items will be used.");
            return MC_V1_13;
        }

        return UNKNOWN;
    }
}
