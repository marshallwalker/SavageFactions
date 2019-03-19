package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FWarp;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.configuration.Configuration;
import com.massivecraft.factions.configuration.implementation.warp.WarpConfiguration;
import com.massivecraft.factions.configuration.implementation.warp.WarpCostConfiguration;
import com.massivecraft.factions.struct.*;
import com.massivecraft.factions.util.LazyLocation;
import com.massivecraft.factions.zcore.util.TL;

public class CmdSetFWarp extends FCommand {
    private final WarpConfiguration configuration;

    public CmdSetFWarp() {
        super();

        aliases.add("setwarp");
        aliases.add("sw");

        requiredArgs.add("warp name");

        optionalArgs.put("password", "password");
        optionalArgs.put("newPassword", "newPassword");

        this.permission = Permission.SETWARP.node;
        this.senderMustBeMember = true;
        this.senderMustBePlayer = true;

        this.configuration = SavageFactionsPlugin.plugin.getConfiguration().warps;
    }

    @Override
    public void perform() {
        if (!(fme.getRelationToLocation() == Relation.MEMBER)) {
            fme.msg(TL.COMMAND_SETFWARP_NOTCLAIMED);
            return;
        }

        // This statement allows us to check if they've specifically denied it, or default to
        // the old setting of allowing moderators to set warps.
        if (!fme.isAdminBypassing()) {
            Access access = myFaction.getAccess(fme, PermissableAction.SETWARP);
            if (access != Access.ALLOW && fme.getRole() != Role.LEADER) {
                fme.msg(TL.GENERIC_FPERM_NOPERMISSION, "set warps");
                return;
            }
        }

        // Checks if warp with same name already exists and ignores maxWarp check if it does.

        String warpName = argAsString(0).toLowerCase();
        FWarp theWarp = myFaction.getWarp(warpName);

        int maxWarps = configuration.max;

        if (theWarp != null && maxWarps <= myFaction.getWarps().size()) {
            fme.msg(TL.COMMAND_SETFWARP_LIMIT, maxWarps);
            return;
        }

        if (!transact(fme)) {
            return;
        }

        String password = argAsString(1, "");
        String newPassword = argAsString(2, password);

        if (theWarp == null) {
            LazyLocation loc = new LazyLocation(fme.getPlayer().getLocation());
            myFaction.setWarp(warpName, password, loc);
            fme.msg(TL.COMMAND_SETFWARP_SET, warpName, password != null ? password : "");
            return;
        }

        if (!theWarp.isPassword(password) && (theWarp.hasPassword() || args.size() > 2)) {
            fme.msg(TL.COMMAND_FWARP_INVALID_PASSWORD);
            return;
        }

        fme.msg(TL.COMMAND_SETFWARP_SET, warpName, newPassword != null ? newPassword : "");
        if (!password.equals(newPassword) || (!theWarp.hasPassword() && args.size() < 3)) {
            theWarp.setPassword(newPassword);
        }

        theWarp.setLocation(new LazyLocation(fme.getPlayer().getLocation()));
    }

    private boolean transact(FPlayer player) {
        WarpCostConfiguration warpCost = configuration.cost;

        if(player.isAdminBypassing() || !warpCost.enabled) {
            return true;
        }

        return payForCommand(warpCost.create, TL.COMMAND_SETFWARP_TOSET, TL.COMMAND_SETFWARP_FORSET);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_SETFWARP_DESCRIPTION;
    }
}
