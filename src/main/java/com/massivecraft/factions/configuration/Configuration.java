package com.massivecraft.factions.configuration;

import com.massivecraft.factions.configuration.implementation.faction.FactionConfiguration;
import com.massivecraft.factions.configuration.implementation.upgrade.UpgradesConfiguration;
import com.massivecraft.factions.configuration.implementation.warp.WarpConfiguration;

public class Configuration implements IConfigurable {

    public FactionConfiguration faction = new FactionConfiguration();

    public WarpConfiguration warps = new WarpConfiguration();

    public UpgradesConfiguration upgrades = new UpgradesConfiguration();
}
