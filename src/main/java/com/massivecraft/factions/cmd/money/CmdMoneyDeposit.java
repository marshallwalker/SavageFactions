package com.massivecraft.factions.cmd.money;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.participator.EconomyParticipator;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.ChatColor;


public class CmdMoneyDeposit extends FCommand {

    public CmdMoneyDeposit() {
        super();

        aliases.add("d");
        aliases.add("deposit");

        requiredArgs.add("amount");

        optionalArgs.put("faction", "yours");

        this.permission = Permission.MONEY_DEPOSIT.node;
        this.senderMustBePlayer = true;
    }

    @Override
    public void perform() {
        double amount = argAsDouble(0, 0.0);
        Faction faction = argAsFaction(1, myFaction);

        if (faction.isWilderness()) {
            return;
        }

        fme.msg(TL.COMMAND_MONEY_DEPOSIT_DEPOSITOR,  Econ.moneyString(amount), faction.getBalanceFormatted());
        fme.transfer(amount, faction);

        //	boolean success = Econ.transferMoney(fme, fme, faction, amount);

        //	if (success && Conf.logMoneyTransactions) {
        SavageFactionsPlugin.plugin.log(ChatColor.stripColor(SavageFactionsPlugin.plugin.txt.parse(TL.COMMAND_MONEYDEPOSIT_DEPOSITED.toString(), fme.getName(), Econ.moneyString(amount), faction.describeTo(null))));
        //	}
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_MONEYDEPOSIT_DESCRIPTION;
    }

}
