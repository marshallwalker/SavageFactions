package com.massivecraft.factions.zcore.persist.json;

import com.massivecraft.factions.zcore.persist.MemoryFaction;

import java.util.UUID;

public class JSONFaction extends MemoryFaction {

	public JSONFaction(MemoryFaction arg0) {
		super(arg0);
	}

	public JSONFaction() {
	}

	public JSONFaction(UUID id, String tag) {
		super(id);

		this.tag = tag;
	}
}
