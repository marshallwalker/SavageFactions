package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.participator.EconomyParticipator;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class CmdMoneyTransferFf extends FCommand {

	public CmdMoneyTransferFf() {
		this.aliases.add("ff");

		this.requiredArgs.add("amount");
		this.requiredArgs.add("faction");
		this.requiredArgs.add("faction");

		//this.optionalArgs.put("", "");

		this.permission = Permission.MONEY_F2F.node;


		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeModerator = false;
		senderMustBeColeader = false;
		senderMustBeAdmin = false;
	}

	@Override
	public void perform() {
		double amount = this.argAsDouble(0, 0d);
		EconomyParticipator from = this.argAsFaction(1);
		if (from == null) {
			return;
		}
		EconomyParticipator to = this.argAsFaction(2);
		if (to == null) {
			return;
		}

		boolean success = Econ.transferMoney(fme, from, to, amount);

		if (success && Conf.logMoneyTransactions) {
			String name = sender instanceof Player ? fme.getName() : sender.getName();
			SavageFactionsPlugin.plugin.log(ChatColor.stripColor(SavageFactionsPlugin.plugin.txt.parse(TL.COMMAND_MONEYTRANSFERFF_TRANSFER.toString(), name, Econ.moneyString(amount), from.describeTo(null), to.describeTo(null))));
		}
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_MONEYTRANSFERFF_DESCRIPTION;
	}
}
