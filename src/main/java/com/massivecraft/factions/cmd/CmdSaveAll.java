package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.configuration.Configuration;
import com.massivecraft.factions.configuration.ConfigurationBuilder;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;

import java.io.File;
import java.io.IOException;

public class CmdSaveAll extends FCommand {

    public CmdSaveAll() {
        super();

        aliases.add("saveall");
        aliases.add("save");

        this.permission = Permission.SAVE.node;
        this.disableOnLock = false;
    }

    @Override
    public void perform() {
        ConfigurationBuilder configurationBuilder = ConfigurationBuilder.getInstance();
        Configuration configuration = SavageFactionsPlugin.plugin.getConfiguration();

        try {
            configurationBuilder.from(configuration)
                    .to(p.getConfigurationFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FPlayers.getInstance().save();
            Factions.getInstance().save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Board.getInstance().forceSave(false);
        Conf.save();
        msg(TL.COMMAND_SAVEALL_SUCCESS);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_SAVEALL_DESCRIPTION;
    }

}