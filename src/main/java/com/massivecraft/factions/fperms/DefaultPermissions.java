package com.massivecraft.factions.fperms;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class DefaultPermissions {
	public boolean ban;
	public boolean build;
	public boolean destroy;
	public boolean frostwalk;
	public boolean painbuild;
	public boolean door;
	public boolean button;
	public boolean lever;
	public boolean container;
	public boolean invite;
	public boolean kick;
	public boolean items;
	public boolean sethome;
	public boolean territory;
	public boolean access;
	public boolean home;
	public boolean disband;
	public boolean promote;
	public boolean setwarp;
	public boolean warp;
	public boolean fly;
	public boolean vault;
	public boolean tntbank;
	public boolean tntfill;
	public boolean withdraw;
	public boolean chest;
	public boolean spawner;

	public DefaultPermissions(boolean def) {
		this.ban = def;
		this.build = def;
		this.destroy = def;
		this.frostwalk = def;
		this.painbuild = def;
		this.door = def;
		this.button = def;
		this.lever = def;
		this.container = def;
		this.invite = def;
		this.kick = def;
		this.items = def;
		this.sethome = def;
		this.territory = def;
		this.access = def;
		this.home = def;
		this.disband = def;
		this.promote = def;
		this.setwarp = def;
		this.warp = def;
		this.fly = def;
		this.vault = def;
		this.tntbank = def;
		this.tntfill = def;
		this.withdraw = def;
		this.chest = def;
		this.spawner = def;
	}

	@Deprecated
	public boolean getbyName(String name) {
		if (name == "ban") return this.ban;
		else if (name == "buildButton") return this.build;
		else if (name == "destroy") return this.destroy;
		else if (name == "frostwalk") return this.frostwalk;
		else if (name == "painbuild") return this.painbuild;
		else if (name == "door") return this.door;
		else if (name == "button") return this.button;
		else if (name == "lever") return this.lever;
		else if (name == "container") return this.container;
		else if (name == "invite") return this.invite;
		else if (name == "kick") return this.kick;
		else if (name == "items") return this.items;
		else if (name == "sethome") return this.sethome;
		else if (name == "territory") return this.territory;
		else if (name == "access") return this.access;
		else if (name == "home") return this.disband;
		else if (name == "disband") return this.disband;
		else if (name == "promote") return this.promote;
		else if (name == "setwarp") return this.setwarp;
		else if (name == "warp") return this.warp;
		else if (name == "fly") return this.fly;
		else if (name == "vault") return this.vault;
		else if (name == "tntbank") return this.tntbank;
		else if (name == "tntfill") return this.tntfill;
		else if (name == "withdraw") return this.withdraw;
		else if (name == "chest") return this.chest;
		else if (name == "spawner") return this.spawner;
		else return false;
	}
}
