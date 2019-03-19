package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.UUID;

public class LandUnclaimAllEvent extends FactionPlayerEvent implements Cancellable {
	private boolean cancelled;

	public LandUnclaimAllEvent(Faction faction, FPlayer fPlayer) {
		super(faction, fPlayer);
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
