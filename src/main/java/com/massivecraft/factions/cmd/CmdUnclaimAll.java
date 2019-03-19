package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.configuration.Configuration;
import com.massivecraft.factions.configuration.implementation.faction.BankConfiguration;
import com.massivecraft.factions.event.LandUnclaimAllEvent;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;

public class CmdUnclaimAll extends FCommand {

	public CmdUnclaimAll() {
		aliases.add("unclaimall");
		aliases.add("declaimall");


		this.permission = Permission.UNCLAIM_ALL.node;
		this.senderMustBePlayer = true;
		this.senderMustBeModerator = true;

	}

	@Override
	public void perform() {
		if (Econ.shouldBeUsed()) {
			double refund = Econ.calculateTotalLandRefund(myFaction.getLandRounded());

			Configuration configuration = SavageFactionsPlugin.plugin.getConfiguration();
			BankConfiguration bankConfiguration = configuration.faction.bank;

			if (bankConfiguration.enabled && bankConfiguration.factionPaysLandCost) {
				if (!Econ.modifyMoney(myFaction, refund, TL.COMMAND_UNCLAIMALL_TOUNCLAIM.toString(), TL.COMMAND_UNCLAIMALL_FORUNCLAIM.toString())) {
					return;
				}
			} else {
				if (!Econ.modifyMoney(fme, refund, TL.COMMAND_UNCLAIMALL_TOUNCLAIM.toString(), TL.COMMAND_UNCLAIMALL_FORUNCLAIM.toString())) {
					return;
				}
			}
		}

		LandUnclaimAllEvent unclaimAllEvent = new LandUnclaimAllEvent(myFaction, fme);
		Bukkit.getServer().getPluginManager().callEvent(unclaimAllEvent);
		if (unclaimAllEvent.isCancelled()) {
			return;
		}

		Board.getInstance().unclaimAll(myFaction.getUniqueId());
		myFaction.msg(TL.COMMAND_UNCLAIMALL_UNCLAIMED, fme.describeTo(myFaction, true));

		if (Conf.logLandUnclaims) {
			SavageFactionsPlugin.plugin.log(TL.COMMAND_UNCLAIMALL_LOG.format(fme.getName(), myFaction.getTag()));
		}
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_UNCLAIMALL_DESCRIPTION;
	}
}
