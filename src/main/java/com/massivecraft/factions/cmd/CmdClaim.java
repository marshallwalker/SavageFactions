package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.SpiralTask;
import com.massivecraft.factions.struct.Access;
import com.massivecraft.factions.struct.PermissableAction;
import com.massivecraft.factions.zcore.util.TL;


public class CmdClaim extends FCommand {

	public CmdClaim() {
		super();
		this.aliases.add("claim");

		//this.requiredArgs.add("");
		this.optionalArgs.put("radius", "1");
		this.optionalArgs.put("faction", "your");

		this.permission = Permission.CLAIM.node;
		this.disableOnLock = true;

		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeModerator = false;
		senderMustBeColeader = false;
		senderMustBeAdmin = false;
	}

	@Override
	public void perform() {
		// Read and validate input
		int radius = this.argAsInt(0, 1); // Default to 1
		final Faction forFaction = this.argAsFaction(1, myFaction); // Default to own

		if (!fme.isAdminBypassing()) {
			Access access = myFaction.getAccess(fme, PermissableAction.TERRITORY);
			if (access != Access.ALLOW && fme.getRole() != Role.LEADER) {
				fme.msg(TL.GENERIC_FPERM_NOPERMISSION, "change faction territory");
				return;
			}
		}

		if (forFaction.isWilderness()) {
			CmdUnclaim cmdUnclaim = SavageFactionsPlugin.plugin.cmdBase.cmdUnclaim;
			cmdUnclaim.execute(sender, args.size() > 1 ? args.subList(0, 1) : args);
			return;
		}

		if (radius < 1) {
			msg(TL.COMMAND_CLAIM_INVALIDRADIUS);
			return;
		}

		if (radius < 2) {
			// single chunk
			fme.attemptClaim(forFaction, me.getLocation(), true);
		} else {
			// radius claim
			if (!Permission.CLAIM_RADIUS.has(sender, false)) {
				msg(TL.COMMAND_CLAIM_DENIED);
				return;
			}

			new SpiralTask(new FLocation(me), radius) {
				private final int limit = Conf.radiusClaimFailureLimit - 1;
				private int failCount = 0;

				@Override
				public boolean work() {
					boolean success = fme.attemptClaim(forFaction, this.currentLocation(), true);
					if (success) {
						failCount = 0;
					} else if (failCount++ >= limit) {
						this.stop();
						return false;
					}

					return true;
				}
			};
		}
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_CLAIM_DESCRIPTION;
	}

}
