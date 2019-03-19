package com.massivecraft.factions.cmd.warp;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FWarp;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.configuration.Configuration;
import com.massivecraft.factions.configuration.implementation.warp.WarpConfiguration;
import com.massivecraft.factions.configuration.implementation.warp.WarpCostConfiguration;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.inventory.menu.WarpMenu;
import com.massivecraft.factions.struct.Access;
import com.massivecraft.factions.struct.PermissableAction;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.WarmUpUtil;
import com.massivecraft.factions.zcore.util.TL;

public class CmdWarp extends FCommand {

    public CmdWarp() {
        super();

        aliases.add("warp");
        aliases.add("warps");

        optionalArgs.put("warpname", "warpname");
        optionalArgs.put("password", "password");

        this.permission = Permission.WARP.node;
        this.senderMustBeMember = true;
    }

    @Override
    public void perform() {

        //TODO: check if in combat.
        if (!fme.isAdminBypassing()) {
            Access access = myFaction.getAccess(fme, PermissableAction.WARP);
            if (access != Access.ALLOW && fme.getRole() != Role.LEADER) {
                fme.msg(TL.GENERIC_FPERM_NOPERMISSION, "use warps");
                return;
            }
        }

        if (args.size() == 0) {
            new WarpMenu(SavageFactionsPlugin.plugin).show(me);
            return;
        }

        if (args.size() > 2) {
            fme.msg(TL.COMMAND_FWARP_COMMANDFORMAT);
            return;
        }

        String warpName = argAsString(0).toLowerCase();
        FWarp warp = myFaction.getWarp(warpName);
        String passwordAttempt = argAsString(1, "");

        if (warp == null) {
            fme.msg(TL.COMMAND_FWARP_INVALID_WARP, warpName);
            return;
        }

        if (!warp.isPassword(passwordAttempt)) {
            fme.msg(TL.COMMAND_FWARP_INVALID_PASSWORD);
            return;
        }

        if (!attemptTransaction(fme)) {
            return;
        }

        doWarmUp(WarmUpUtil.Warmup.WARP, TL.WARMUPS_NOTIFY_TELEPORT, warpName, () -> {
            me.teleport(fme.getFaction().getWarp(warpName).getLocation().getLocation());
            fme.msg(TL.COMMAND_FWARP_WARPED, warpName);

        }, p.getConfig().getLong("warmups.f-warp", 0));
    }

    private boolean attemptTransaction(FPlayer fplayer) {
        Configuration configuration = p.getConfiguration();
        WarpConfiguration warps = configuration.warps;
        WarpCostConfiguration warpCosts = warps.cost;

        if (!warpCosts.enabled || fplayer.isAdminBypassing()) {
            return true;
        }

        return Econ.attemptPayment(fplayer, warpCosts.use, TL.COMMAND_FWARP_TOWARP, TL.COMMAND_FWARP_FORWARPING);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_FWARP_DESCRIPTION;
    }
}
