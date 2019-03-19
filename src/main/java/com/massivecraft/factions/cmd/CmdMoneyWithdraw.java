package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Access;
import com.massivecraft.factions.struct.PermissableAction;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.ChatColor;


public class CmdMoneyWithdraw extends FCommand {

	public CmdMoneyWithdraw() {
		this.aliases.add("w");
		this.aliases.add("withdraw");

		this.requiredArgs.add("amount");
		this.optionalArgs.put("faction", "yours");

		this.permission = Permission.MONEY_WITHDRAW.node;


		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeModerator = false;
		senderMustBeColeader = false;
		senderMustBeAdmin = false;
	}

	@Override
	public void perform() {
		double amount = this.argAsDouble(0, 0.0);

		Faction faction = this.argAsFaction(1, myFaction);

		if (faction == null) {
			return;
		}

		Access access = myFaction.getAccess(fme, PermissableAction.WITHDRAW);

		if (!fme.isAdminBypassing() || access == Access.DENY) {
			fme.msg(TL.GENERIC_NOPERMISSION, "withdraw");
			return;
		}

		if(faction.canAfford(amount)) {
			faction.transfer(amount, fme);

			SavageFactionsPlugin.plugin.log(ChatColor.stripColor(SavageFactionsPlugin.plugin.txt.parse(TL.COMMAND_MONEYWITHDRAW_WITHDRAW.toString(), fme.getName(), Econ.moneyString(amount), faction.describeTo(null))));
		}

//		boolean success = Econ.transferMoney(fme, faction, fme, amount);
//
//		if (success && Conf.logMoneyTransactions) {
//			SavageFactionsPlugin.plugin.log(ChatColor.stripColor(SavageFactionsPlugin.plugin.txt.parse(TL.COMMAND_MONEYWITHDRAW_WITHDRAW.toString(), fme.getName(), Econ.moneyString(amount), faction.describeTo(null))));
//		}
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_MONEYWITHDRAW_DESCRIPTION;
	}
}
