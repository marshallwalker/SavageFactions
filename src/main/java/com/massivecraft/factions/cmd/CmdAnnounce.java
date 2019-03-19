package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CmdAnnounce extends FCommand {

    public CmdAnnounce() {
        super();

        aliases.add("ann");
        aliases.add("announce");

        requiredArgs.add("message");

        this.errorOnToManyArgs = false;

        this.permission = Permission.ANNOUNCE.node;
        this.disableOnLock = false;

        this.senderMustBePlayer = true;
        this.senderMustBeMember = true;
    }

    @Override
    public void perform() {
        String prefix = ChatColor.GREEN + myFaction.getTag() + ChatColor.YELLOW + " [" + ChatColor.GRAY + me.getName() + ChatColor.YELLOW + "] " + ChatColor.RESET;
        String message = StringUtils.join(args, " ");

        for (Player player : myFaction.getOnlinePlayers()) {
            player.sendMessage(prefix + message);
        }

        // Add for offline players.
        for (FPlayer fp : myFaction.getFPlayersWhereOnline(false)) {
            myFaction.addAnnouncement(fp, prefix + message);
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_ANNOUNCE_DESCRIPTION;
    }
}
