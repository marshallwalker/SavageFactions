package com.massivecraft.factions.event;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Event called when an FPlayer unclaims land for a Faction.
 */
public class LandUnclaimEvent extends FactionPlayerEvent implements Cancellable {
	private final FLocation location;
	private boolean cancelled;

	public LandUnclaimEvent(FLocation location, Faction faction, FPlayer fPlayer) {
		super(faction, fPlayer);

		this.location = location;
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
