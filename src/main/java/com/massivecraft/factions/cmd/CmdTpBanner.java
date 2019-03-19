package com.massivecraft.factions.cmd;

import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.WarmUpUtil;
import com.massivecraft.factions.zcore.util.TL;

public class CmdTpBanner extends FCommand {

    public CmdTpBanner() {
        super();

        aliases.add("tpbanner");

        this.permission = Permission.TPBANNER.node;
        this.senderMustBePlayer = true;
        this.senderMustBeMember = true;

    }

    @Override
    public void perform() {
        if (!SavageFactionsPlugin.plugin.getConfig().getBoolean("fbanners.Enabled")) {
            return;
        }

        if (FactionsBlockListener.warBanners.containsKey(fme.getFactionId())) {
            fme.msg(TL.COMMAND_TPBANNER_SUCCESS);

            doWarmUp(WarmUpUtil.Warmup.BANNER, TL.WARMUPS_NOTIFY_TELEPORT, "Banner", () ->
                    me.teleport(FactionsBlockListener.warBanners.get(fme.getFactionId()).getLocation()), this.p.getConfig().getLong("warmups.f-banner", 0));
        } else {
            fme.msg(TL.COMMAND_TPBANNER_NOTSET);
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_TPBANNER_DESCRIPTION;
    }
}
