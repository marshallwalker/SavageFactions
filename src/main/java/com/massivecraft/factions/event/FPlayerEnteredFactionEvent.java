package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import org.bukkit.event.HandlerList;

public class FPlayerEnteredFactionEvent extends FactionPlayerEvent {
	private static final HandlerList handlers = new HandlerList();

	private final Faction factionTo;
	private final Faction factionFrom;
	private final FPlayer fPlayer;

	public FPlayerEnteredFactionEvent(Faction factionTo, Faction factionFrom, FPlayer fPlayer) {
		super(fPlayer.getFaction(), fPlayer);

		this.factionTo = factionTo;
		this.factionFrom = factionFrom;
		this.fPlayer = fPlayer;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	@Override
	public FPlayer getFPlayer() {
		return fPlayer;
	}

	public Faction getFactionTo() {
		return factionTo;
	}

	public Faction getFactionFrom() {
		return factionFrom;
	}
}
