package com.massivecraft.factions.configuration;

import com.google.common.collect.Lists;
import com.massivecraft.factions.configuration.implementation.faction.FactionConfiguration;
import com.massivecraft.factions.configuration.implementation.upgrade.UpgradesConfiguration;
import com.massivecraft.factions.configuration.implementation.warp.WarpConfiguration;

import java.util.List;

public class Configuration implements IConfigurable {

    public List<String> baseCommands = Lists.newArrayList("f");

    public FactionConfiguration faction = new FactionConfiguration();

    public WarpConfiguration warps = new WarpConfiguration();

    public UpgradesConfiguration upgrades = new UpgradesConfiguration();
}
