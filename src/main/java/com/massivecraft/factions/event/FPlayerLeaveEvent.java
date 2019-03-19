package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import org.bukkit.event.Cancellable;

public class FPlayerLeaveEvent extends FactionPlayerEvent implements Cancellable {
	private final PlayerLeaveReason reason;
	private boolean cancelled;

	public FPlayerLeaveEvent(FPlayer fPlayer, Faction faction, PlayerLeaveReason reason) {
		super(faction, fPlayer);

		this.reason = reason;
	}

	public PlayerLeaveReason getReason() {
		return reason;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		// Don't let them cancel factions disbanding.
		this.cancelled = reason != PlayerLeaveReason.DISBAND && reason != PlayerLeaveReason.RESET && cancelled;
	}

	public enum PlayerLeaveReason {
		KICKED,
		DISBAND,
		RESET,
		JOIN_OTHER,
		LEAVE,
		BANNED
	}
}