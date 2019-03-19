package com.massivecraft.factions.cmd;

import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.ChatColor;

import java.util.List;

public class CmdRules extends FCommand {

    public CmdRules() {
        super();

        aliases.add("r");
        aliases.add("rule");
        aliases.add("rules");

        this.optionalArgs.put("add/remove/set/clear", "");
        this.errorOnToManyArgs = false;

        this.permission = Permission.RULES.node;

        this.senderMustBePlayer = true;
        this.senderMustBeMember = true;
    }

    @Override
    public void perform() {
        if (!SavageFactionsPlugin.plugin.getConfig().getBoolean("frules.Enabled")) {
            fme.msg(TL.COMMAND_RULES_DISABLED_MSG);
            return;
        }

        List<String> rules = fme.getFaction().getRules();

        if (args.size() == 0) {
            if (rules.size() == 0) {
                List<String> ruleList = SavageFactionsPlugin.plugin.getConfig().getStringList("frules.default-rules");
                fme.sendMessage(SavageFactionsPlugin.plugin.colorList(ruleList));

                return;
            }

            for (String rule : rules) {
                fme.sendMessage(ChatColor.translateAlternateColorCodes('&', rule));
            }

            return;
        }

        if (this.args.size() == 1) {
            if (args.get(0).equalsIgnoreCase("add")) {
                fme.msg(TL.COMMAND_RULES_ADD_INVALIDARGS);
            }
            if (args.get(0).equalsIgnoreCase("set")) {
                fme.msg(TL.COMMAND_RULES_SET_INVALIDARGS);
            }
            if (args.get(0).equalsIgnoreCase("remove")) {
                fme.msg(TL.COMMAND_RULES_REMOVE_INVALIDARGS);
            }
            if (args.get(0).equalsIgnoreCase("clear")) {
                fme.getFaction().clearRules();
                fme.msg(TL.COMMAND_RULES_CLEAR_SUCCESS);
            }
        }

        if (this.args.size() >= 2) {
            if (args.get(0).equalsIgnoreCase("add")) {
                String message = "";
                StringBuilder string = new StringBuilder(message);
                for (int i = 1; i <= args.size() - 1; i++) {
                    string.append(" " + args.get(i));
                }
                fme.getFaction().addRule(string.toString());
                fme.msg(TL.COMMAND_RULES_ADD_SUCCESS);
            }

            if (this.args.size() == 2) {
                if (args.get(0).equalsIgnoreCase("remove")) {
                    int index = argAsInt(1);

                    if (index < 0 || index > rules.size()) {
                        return;
                    }

                    fme.getFaction().removeRule(index);
                    fme.msg(TL.COMMAND_RULES_REMOVE_SUCCESS);
                }
            }
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_RULES_DESCRIPTION;
    }
}
