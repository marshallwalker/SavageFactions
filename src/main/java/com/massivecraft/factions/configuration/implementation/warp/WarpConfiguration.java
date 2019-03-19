package com.massivecraft.factions.configuration.implementation.warp;

import com.massivecraft.factions.configuration.implementation.MenuConfiguration;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class WarpConfiguration {
    public boolean enabled = true;

    public MenuConfiguration menu = new MenuConfiguration("Faction Warps", 27);

    public WarpCostConfiguration cost = new WarpCostConfiguration(false, 5.0, 5.0, 5.0);

    public int max = 5;
    public int warmup = 10;
}
