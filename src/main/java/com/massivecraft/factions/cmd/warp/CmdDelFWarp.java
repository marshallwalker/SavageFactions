package com.massivecraft.factions.cmd.warp;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.FWarp;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;

public class CmdDelFWarp extends FCommand {

    public CmdDelFWarp() {
        super();
        this.aliases.add("delwarp");
        this.aliases.add("dw");
        this.aliases.add("deletewarp");
        this.requiredArgs.add("warp name");
        optionalArgs.put("password", "password");
        this.senderMustBeMember = true;
        this.senderMustBeModerator = true;
        this.senderMustBePlayer = true;
        this.permission = Permission.SETWARP.node;
    }

    @Override
    public void perform() {
        String warpName = argAsString(0).toLowerCase();
        String password = argAsString(1, "");
        FWarp theWarp = myFaction.getWarp(warpName);

        if (theWarp == null) {
            fme.msg(TL.COMMAND_DELFWARP_INVALID, warpName);
            return;
        }

        if (!transact(fme)) {
            return;
        }

        if(theWarp.isPassword(password) || fme.getRole().value >= Role.COLEADER.value) {
            fme.msg(TL.COMMAND_DELFWARP_DELETED, warpName);
            myFaction.removeWarp(warpName);
            return;
        }

        fme.msg(TL.COMMAND_FWARP_INVALID_PASSWORD);
    }

    private boolean transact(FPlayer player) {
        return !SavageFactionsPlugin.plugin.getConfig().getBoolean("warp-cost.enabled", false) || player.isAdminBypassing() || payForCommand(SavageFactionsPlugin.plugin.getConfig().getDouble("warp-cost.delwarp", 5), TL.COMMAND_DELFWARP_TODELETE, TL.COMMAND_DELFWARP_FORDELETE);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_DELFWARP_DESCRIPTION;
    }
}
