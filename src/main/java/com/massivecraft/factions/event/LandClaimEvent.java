package com.massivecraft.factions.event;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Event called when an FPlayer claims land for a Faction.
 */
public class LandClaimEvent extends FactionPlayerEvent implements Cancellable {
	private final FLocation location;

	private boolean cancelled;

	public LandClaimEvent(FLocation fLocation, Faction faction, FPlayer fPlayer) {
		super(faction, fPlayer);

		this.location = fLocation;
	}

	public FLocation getLocation() {
		return this.location;
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
