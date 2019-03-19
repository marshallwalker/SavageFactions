package com.massivecraft.factions.inventory.menu.component;

import com.massivecraft.factions.inventory.menu.MenuEvent;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class DataButton<T> implements Button {
    private final T data;
    private final BiFunction<T, MenuEvent, ItemStack> displayBuilder;
    private final BiConsumer<T, MenuEvent> action;

    @Override
    public ItemStack getDisplay(MenuEvent menuEvent) {
        return displayBuilder.apply(data, menuEvent);
    }

    @Override
    public void preform(MenuEvent menuEvent) {
        action.accept(data, menuEvent);
    }
}
