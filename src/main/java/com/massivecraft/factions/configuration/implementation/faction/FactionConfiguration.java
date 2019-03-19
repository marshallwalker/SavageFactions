package com.massivecraft.factions.configuration.implementation.faction;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FactionConfiguration {

    public PowerConfiguration power = new PowerConfiguration();

    public RelationConfiguration relation = new RelationConfiguration();

    //Faction banks, to pay for land claiming and other costs instead of individuals paying for them
    public BankConfiguration bank = new BankConfiguration();


}
