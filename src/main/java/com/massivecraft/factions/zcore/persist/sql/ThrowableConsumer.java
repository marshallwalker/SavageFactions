package com.massivecraft.factions.zcore.persist.sql;

@FunctionalInterface
public interface ThrowableConsumer<T, E extends Throwable> {

    void accept(T value) throws E;
}
