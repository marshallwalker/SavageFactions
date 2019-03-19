package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Factions;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.UUID;

/**
 * Event called when a faction is disbanded.
 */
public class FactionDisbandEvent extends FactionEvent implements Cancellable {
	private final FPlayer fPlayer;
	private final PlayerDisbandReason reason;

	private boolean cancelled;

	public FactionDisbandEvent(FPlayer fplayer, UUID factionId, PlayerDisbandReason reason) {
		super(Factions.getInstance().getFactionById(factionId));

		this.fPlayer = fplayer;
		this.reason = reason;
	}

	public FPlayer getFPlayer() {
		return fPlayer;
	}

	public PlayerDisbandReason getReason() {
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

	public enum PlayerDisbandReason {
		COMMAND,
		PLUGIN,
		INACTIVITY,
		LEAVE
	}
}
