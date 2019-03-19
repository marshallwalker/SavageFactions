package com.massivecraft.factions.configuration.implementation.upgrade;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.massivecraft.factions.struct.Upgrade;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UpgradesConfiguration {
    public boolean enabled = true;

    public UpgradeConfiguration crops = new UpgradeConfiguration(10, new ItemStack(Material.BEDROCK),
            new UpgradeLevel(10_000_000, 0.3),
            new UpgradeLevel(25_000_000, 0.4),
            new UpgradeLevel(50_000_000, 0.6));

    public UpgradeConfiguration spawners = new UpgradeConfiguration(13, new ItemStack(Material.BEDROCK),
            new UpgradeLevel(10_000_000, 1.1),
            new UpgradeLevel(25_000_000, 1.15),
            new UpgradeLevel(50_000_000, 1.3));

    public UpgradeConfiguration mcmmo = new UpgradeConfiguration(16, new ItemStack(Material.BEDROCK),
            new UpgradeLevel(50_000_000, 2),
            new UpgradeLevel(25_000_000, 3));

    public UpgradeConfiguration slots = new UpgradeConfiguration(22, new ItemStack(Material.BEDROCK),
            new UpgradeLevel(25_000_000, 10),
            new UpgradeLevel(50_000_000, 15),
            new UpgradeLevel(100_000_000, 20),
            new UpgradeLevel(150_000_000, 25));

    @JsonIgnore
    public UpgradeConfiguration getUpgrade(Upgrade upgrade) {
        switch (upgrade) {
            case CROP:
                return crops;
            case SPAWNER:
                return spawners;
            case MCMMO:
                return mcmmo;
            case SLOTS:
                return slots;
            default:
                return null;
        }
    }

    @JsonIgnore
    public boolean isEnabled(Upgrade upgrade) {
        if (!enabled) {
            return false;
        }

        switch (upgrade) {
            case CROP:
                return crops.enabled;
            case SPAWNER:
                return spawners.enabled;
            case MCMMO:
                return mcmmo.enabled;
            case SLOTS:
                return slots.enabled;
            default:
                return false;
        }
    }
}
