package com.massivecraft.factions.configuration.implementation.faction;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BankConfiguration {
    public boolean enabled = true;

    public boolean membersCanWithdraw;

    public boolean factionPaysCosts = true;
    public boolean factionPaysLandCost = true;
}