package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a Faction is created.
 */
public class FactionCreateEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final String factionTag;
	private final FPlayer sender;

	private boolean cancelled;

	public FactionCreateEvent(FPlayer sender, String tag) {
		this.factionTag = tag;
		this.sender = sender;
		this.cancelled = false;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public String getFactionTag() {
		return factionTag;
	}

	public FPlayer getFPlayer() {
		return sender;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}