package com.massivecraft.factions.inventory.menu.component;

import com.massivecraft.factions.inventory.menu.MenuEvent;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class SimpleButton implements Button {
    private final Function<MenuEvent, ItemStack> displayBuilder;
    private final Consumer<MenuEvent> action;

    @Override
    public ItemStack getDisplay(MenuEvent menuEvent) {
        return displayBuilder.apply(menuEvent);
    }

    @Override
    public void preform(MenuEvent menuEvent) {
        action.accept(menuEvent);
    }
}
