package com.massivecraft.factions;

import org.bukkit.Location;

public interface IWarBanner {

    void spawn();

    void remove();

    void update();

    Location getLocation();

    long getAge();
}
