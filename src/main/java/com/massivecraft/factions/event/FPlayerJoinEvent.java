package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import org.bukkit.event.Cancellable;

/**
 * Event called when an FPlayer joins a Faction.
 */
public class FPlayerJoinEvent extends FactionPlayerEvent implements Cancellable {
	private final PlayerJoinReason reason;
	private boolean cancelled;

	public FPlayerJoinEvent(FPlayer fPlayer, Faction faction, PlayerJoinReason reason) {
		super(faction, fPlayer);

		this.reason = reason;
	}

	public PlayerJoinReason getReason() {
		return reason;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public enum PlayerJoinReason {
		CREATE,
		LEADER,
		COMMAND
	}
}