package com.massivecraft.factions.configuration.implementation.upgrade;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
public class UpgradeConfiguration {
    public boolean enabled = true;

    public int slot;
    public ItemStack display;
    public List<UpgradeLevel> levels;

    @JsonIgnore
    public UpgradeLevel getLevel(int level) {
        return level > getMaxLevel() ? null : levels.get(level);
    }

    @JsonIgnore
    public int getMaxLevel() {
        return levels.size();
    }

    public UpgradeConfiguration(int slot, ItemStack display, UpgradeLevel... levels) {
        this.slot = slot;
        this.display = display;
        this.levels = Arrays.asList(levels);
    }
}
