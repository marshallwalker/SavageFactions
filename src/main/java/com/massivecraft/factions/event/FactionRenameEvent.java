package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class FactionRenameEvent extends FactionPlayerEvent implements Cancellable {
	private final String tag;
	private boolean cancelled;

	public FactionRenameEvent(FPlayer sender, String tag) {
		super(sender.getFaction(), sender);

		this.tag = tag;
	}

	public String getFactionTag() {
		return tag;
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
