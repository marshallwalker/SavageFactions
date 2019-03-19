package com.massivecraft.factions.event;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a Faction relation is called.
 */
public class FactionRelationEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private final Faction senderFaction;
	private final Faction targetFaction;
	private final Relation oldRelation;
	private final Relation newRelation;

	public FactionRelationEvent(Faction senderFaction, Faction targetFaction, Relation oldRelation, Relation newRelation) {
		this.senderFaction = senderFaction;
		this.targetFaction = targetFaction;
		this.oldRelation = oldRelation;
		this.newRelation = newRelation;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public Faction getSenderFaction() {
		return senderFaction;
	}

	public Faction getTargetFaction() {
		return targetFaction;
	}

	public Relation getOldRelation() {
		return oldRelation;
	}

	public Relation getRelation() {
		return newRelation;
	}
}
