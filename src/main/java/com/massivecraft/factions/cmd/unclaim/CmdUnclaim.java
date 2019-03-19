package com.massivecraft.factions.cmd.unclaim;

import com.massivecraft.factions.*;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.configuration.Configuration;
import com.massivecraft.factions.configuration.implementation.faction.BankConfiguration;
import com.massivecraft.factions.event.LandUnclaimEvent;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Access;
import com.massivecraft.factions.struct.PermissableAction;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.SpiralTask;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;

public class CmdUnclaim extends FCommand {

    public CmdUnclaim() {
        this.aliases.add("unclaim");
        this.aliases.add("declaim");

        this.optionalArgs.put("radius", "1");

        this.permission = Permission.UNCLAIM.node;
        this.disableOnLock = true;

        senderMustBePlayer = true;
        senderMustBeMember = false;
        senderMustBeModerator = false;
        senderMustBeAdmin = false;
    }

    @Override
    public void perform() {
        // Read and validate input
        int radius = this.argAsInt(0, 1); // Default to 1

        if (!fme.isAdminBypassing()) {
            Access access = myFaction.getAccess(fme, PermissableAction.TERRITORY);
            if (access != Access.ALLOW && fme.getRole() != Role.LEADER) {
                fme.msg(TL.GENERIC_FPERM_NOPERMISSION, "manage faction territory");
                return;
            }
        }

        if (radius < 1) {
            msg(TL.COMMAND_CLAIM_INVALIDRADIUS);
            return;
        }

        if (radius < 2) {
            // single chunk
            unClaim(new FLocation(me));
        } else {
            // radius claim
            if (!Permission.CLAIM_RADIUS.has(sender, false)) {
                msg(TL.COMMAND_CLAIM_DENIED);
                return;
            }

            new SpiralTask(new FLocation(me), radius) {
                private final int limit = Conf.radiusClaimFailureLimit - 1;
                private int failCount = 0;

                @Override
                public boolean work() {
                    boolean success = unClaim(this.currentFLocation());
                    if (success) {
                        failCount = 0;
                    } else if (failCount++ >= limit) {
                        this.stop();
                        return false;
                    }

                    return true;
                }
            };
        }
    }

    private boolean unClaim(FLocation target) {
        Faction targetFaction = Board.getInstance().getFactionAt(target);
        if (targetFaction.isSafeZone()) {
            if (Permission.MANAGE_SAFE_ZONE.has(sender)) {
                Board.getInstance().removeAt(target);
                msg(TL.COMMAND_UNCLAIM_SAFEZONE_SUCCESS);

                if (Conf.logLandUnclaims) {
                    SavageFactionsPlugin.plugin.log(TL.COMMAND_UNCLAIM_LOG.format(fme.getName(), target.getCoordString(), targetFaction.getTag()));
                }
                return true;
            } else {
                msg(TL.COMMAND_UNCLAIM_SAFEZONE_NOPERM);
                return false;
            }
        } else if (targetFaction.isWarZone()) {
            if (Permission.MANAGE_WAR_ZONE.has(sender)) {
                Board.getInstance().removeAt(target);
                msg(TL.COMMAND_UNCLAIM_WARZONE_SUCCESS);

                if (Conf.logLandUnclaims) {
                    SavageFactionsPlugin.plugin.log(TL.COMMAND_UNCLAIM_LOG.format(fme.getName(), target.getCoordString(), targetFaction.getTag()));
                }
                return true;
            } else {
                msg(TL.COMMAND_UNCLAIM_WARZONE_NOPERM);
                return false;
            }
        }

        if (fme.isAdminBypassing()) {
            LandUnclaimEvent unclaimEvent = new LandUnclaimEvent(target, targetFaction, fme);
            Bukkit.getServer().getPluginManager().callEvent(unclaimEvent);
            if (unclaimEvent.isCancelled()) {
                return false;
            }

            Board.getInstance().removeAt(target);

            targetFaction.msg(TL.COMMAND_UNCLAIM_UNCLAIMED, fme.describeTo(targetFaction, true));
            msg(TL.COMMAND_UNCLAIM_UNCLAIMS);

            if (Conf.logLandUnclaims) {
                SavageFactionsPlugin.plugin.log(TL.COMMAND_UNCLAIM_LOG.format(fme.getName(), target.getCoordString(), targetFaction.getTag()));
            }

            return true;
        }


        if (targetFaction.getAccess(fme, PermissableAction.TERRITORY) == Access.DENY) {
            return false;
        }


        if (!assertHasFaction()) {
            return false;
        }

        if (targetFaction.getAccess(fme, PermissableAction.TERRITORY) != Access.ALLOW && !assertMinRole(Role.MODERATOR)) {
            return false;
        }


        if (myFaction != targetFaction) {
            msg(TL.COMMAND_UNCLAIM_WRONGFACTION);
            return false;
        }


        LandUnclaimEvent unclaimEvent = new LandUnclaimEvent(target, targetFaction, fme);
        Bukkit.getServer().getPluginManager().callEvent(unclaimEvent);
        if (unclaimEvent.isCancelled()) {
            return false;
        }

        if (Econ.shouldBeUsed()) {
            double refund = Econ.calculateClaimRefund(myFaction.getLandRounded());

            Configuration configuration = SavageFactionsPlugin.plugin.getConfiguration();
            BankConfiguration bankConfiguration = configuration.faction.bank;

            if (bankConfiguration.enabled && bankConfiguration.factionPaysLandCost) {
                if (!Econ.modifyMoney(myFaction, refund, TL.COMMAND_UNCLAIM_TOUNCLAIM.toString(), TL.COMMAND_UNCLAIM_FORUNCLAIM.toString())) {
                    return false;
                }
            } else {
                if (!Econ.modifyMoney(fme, refund, TL.COMMAND_UNCLAIM_TOUNCLAIM.toString(), TL.COMMAND_UNCLAIM_FORUNCLAIM.toString())) {
                    return false;
                }
            }
        }

        Board.getInstance().removeAt(target);
        myFaction.msg(TL.COMMAND_UNCLAIM_FACTIONUNCLAIMED, fme.describeTo(myFaction, true));

        if (Conf.logLandUnclaims) {
            SavageFactionsPlugin.plugin.log(TL.COMMAND_UNCLAIM_LOG.format(fme.getName(), target.getCoordString(), targetFaction.getTag()));
        }

        return true;
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_UNCLAIM_DESCRIPTION;
    }

}
