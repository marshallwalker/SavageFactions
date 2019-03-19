package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.scoreboards.FTeamWrapper;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;

public class CmdFocus extends FCommand {

    public CmdFocus() {
        aliases.add("focus");

        requiredArgs.add("player");

        this.permission = Permission.FOCUS.node;
        this.senderMustBePlayer = true;
    }

    public void perform() {
        if (!SavageFactionsPlugin.plugin.getConfig().getBoolean("ffocus.Enabled")) {
            fme.msg(TL.GENERIC_DISABLED);
            return;
        }

        FPlayer target = argAsFPlayer(0);

        if (target == null) {
            return;
        }

        if (target.getFactionId().equals(myFaction.getUniqueId())) {
            fme.msg(TL.COMMAND_FOCUS_SAMEFACTION);
            return;
        }

        if ((myFaction.getFocused() != null) && (myFaction.getFocused().equalsIgnoreCase(target.getName()))) {
            myFaction.setFocused(null);
            myFaction.msg(TL.COMMAND_FOCUS_NO_LONGER, target.getName());
            FTeamWrapper.updatePrefixes(target.getFaction());
            return;
        }

        myFaction.msg(TL.COMMAND_FOCUS_FOCUSING, target.getName());
        myFaction.setFocused(target.getName());
        FTeamWrapper.updatePrefixes(target.getFaction());
    }

    public TL getUsageTranslation() {
        return TL.COMMAND_FOCUS_DESCRIPTION;
    }
}

