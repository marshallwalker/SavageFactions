package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import org.bukkit.event.HandlerList;

public class FPlayerStoppedFlying extends FactionPlayerEvent {
	private static final HandlerList handlers = new HandlerList();
	private final FPlayer fPlayer;

	public FPlayerStoppedFlying(FPlayer fPlayer) {
		super(fPlayer.getFaction(), fPlayer);

		this.fPlayer = fPlayer;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public FPlayer getFPlayer() {
		return fPlayer;
	}

	public HandlerList getHandlers() {
		return handlers;
	}
}
