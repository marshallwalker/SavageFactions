package com.massivecraft.factions.cmd.claim;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;

public class CmdClaimAt extends FCommand {

	public CmdClaimAt() {
		super();

		aliases.add("claimat");

		requiredArgs.add("world");
		requiredArgs.add("x");
		requiredArgs.add("z");

		this.permission = Permission.CLAIMAT.node;

		this.senderMustBePlayer = true;
		this.senderMustBeMember = true;
	}

	@Override
	public void perform() {
		FLocation location = new FLocation(argAsString(0), argAsInt(1), argAsInt(2));
		fme.attemptClaim(myFaction, location, true);
	}

	@Override
	public TL getUsageTranslation() {
		return null;
	}
}
