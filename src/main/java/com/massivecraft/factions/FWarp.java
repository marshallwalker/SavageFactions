package com.massivecraft.factions;

import com.massivecraft.factions.util.LazyLocation;

public interface FWarp {

    String getName();

    void setName(String name);

    String getPassword();

    void setPassword(String password);

    boolean hasPassword();

    boolean isPassword(String password);

    LazyLocation getLocation();

    void setLocation(LazyLocation location);
}
