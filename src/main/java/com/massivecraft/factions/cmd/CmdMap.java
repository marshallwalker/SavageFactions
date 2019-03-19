package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;


public class CmdMap extends FCommand {

    public CmdMap() {
        super();

        aliases.add("map");

        optionalArgs.put("on/off", "once");

        this.permission = Permission.MAP.node;
        this.disableOnLock = false;
        this.senderMustBePlayer = true;
    }

    @Override
    public void perform() {
        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!payForCommand(Conf.econCostMap, TL.COMMAND_MAP_TOSHOW, TL.COMMAND_MAP_FORSHOW)) {
            return;
        }

        if (argIsSet(0)) {
            if (this.argAsBool(0, !fme.isMapAutoUpdating())) {
                // Turn on

                fme.setMapAutoUpdating(true);
                msg(TL.COMMAND_MAP_UPDATE_ENABLED);
            } else {
                // Turn off
                fme.setMapAutoUpdating(false);
                msg(TL.COMMAND_MAP_UPDATE_DISABLED);
                return;
            }
        }

        sendFancyMessage(Board.getInstance().getMap(fme, new FLocation(fme), me.getLocation().getYaw()));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_MAP_DESCRIPTION;
    }
}
