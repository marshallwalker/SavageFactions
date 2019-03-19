package com.massivecraft.factions.cmd.claim;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;

public class CmdAutoClaim extends FCommand {

	public CmdAutoClaim() {
		super();

		aliases.add("autoclaim");

		optionalArgs.put("faction", "your");

		this.permission = Permission.AUTOCLAIM.node;
		this.senderMustBePlayer = true;
	}

	@Override
	public void perform() {
		Faction targetFaction = argAsFaction(0, myFaction);

		if (targetFaction == null || targetFaction == fme.getAutoClaimFor()) {
			fme.setAutoClaimFor(null);
			msg(TL.COMMAND_AUTOCLAIM_DISABLED);
			return;
		}

		if (!fme.canClaimForFaction(targetFaction)) {
			if (myFaction == targetFaction) {
				msg(TL.COMMAND_AUTOCLAIM_REQUIREDRANK, Role.MODERATOR.getTranslation());
			} else {
				msg(TL.COMMAND_AUTOCLAIM_OTHERFACTION, targetFaction.describeTo(fme));
			}

			return;
		}

		fme.setAutoClaimFor(targetFaction);

		msg(TL.COMMAND_AUTOCLAIM_ENABLED, targetFaction.describeTo(fme));
		fme.attemptClaim(targetFaction, me.getLocation(), true);
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_AUTOCLAIM_DESCRIPTION;
	}
}