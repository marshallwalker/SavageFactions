package com.massivecraft.factions.inventory.menu.component;

import com.massivecraft.factions.inventory.menu.MenuEvent;
import org.bukkit.inventory.ItemStack;

public interface Button {

    default boolean shouldUpdate(MenuEvent menuEvent) {
        return true;
    }

    ItemStack getDisplay(MenuEvent menuEvent);

    void preform(MenuEvent menuEvent);
}
