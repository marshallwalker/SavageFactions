package com.massivecraft.factions.configuration.implementation.faction;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PowerConfiguration {

    public double factionMax = 0.0;

    public double playerMin = -10;
    public double playerMax = 10;
    public double playerStart = 0.0;

    public double perMinute = 0.2;
    public double perDeath = 4.0;

    public boolean regenOffline= false;

    public double offlineLossPerDay = 0.0;
    public double offlineLossLimit = 0.0;
}
