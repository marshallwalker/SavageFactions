package com.massivecraft.factions.cmd;

import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;

public class CmdPaypalSet extends FCommand {

	public CmdPaypalSet() {
		aliases.add("setpaypal");
		aliases.add("paypal");

		requiredArgs.add("email");

		this.permission = Permission.PAYPALSET.node;

		this.senderMustBePlayer = true;
		this.senderMustBeColeader = true;

		this.disableOnLock = false;
		this.senderMustBeMember = false;
		this.senderMustBeModerator = false;
		this.senderMustBeAdmin = false;

	}

	public void perform() {
		if (!SavageFactionsPlugin.plugin.getConfig().getBoolean("fpaypal.Enabled")) {
			fme.msg(TL.GENERIC_DISABLED);
		} else {
			String paypal = argAsString(0);
			if (paypal != null) {
				myFaction.paypalSet(paypal);
				fme.msg(TL.COMMAND_PAYPALSET_SUCCESSFUL, paypal);
			}
		}
	}

	public TL getUsageTranslation() {
		return TL.COMMAND_PAYPALSET_DESCRIPTION;
	}
}

