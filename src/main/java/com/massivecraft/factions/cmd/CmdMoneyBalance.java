package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;

public class CmdMoneyBalance extends FCommand {

    public CmdMoneyBalance() {
        super();

        aliases.add("b");
        aliases.add("balance");

        optionalArgs.put("faction", "yours");

        this.permission = Permission.MONEY_BALANCE.node;

        setHelpShort(TL.COMMAND_MONEYBALANCE_SHORT.toString());

    }

    @Override
    public void perform() {
        Faction faction = argAsFaction(0, myFaction);

        if (faction == null) {
            return;
        }

        if (faction != myFaction && !Permission.MONEY_BALANCE_ANY.has(sender, true)) {
            return;
        }

        me.sendMessage(faction.getBalanceFormatted());
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_MONEYBALANCE_DESCRIPTION;
    }
}