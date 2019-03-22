package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.configuration.implementation.faction.RuleConfiguration;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.ChatColor;

import java.util.List;

public class CmdRules extends FCommand {
    private final RuleConfiguration configuration;

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

        this.configuration = SavageFactionsPlugin.plugin.getConfiguration().faction.rule;
    }

    @Override
    public void perform() {
        if (!configuration.enabled) {
            fme.msg(TL.COMMAND_RULES_DISABLED_MSG);
            return;
        }

        Faction faction = fme.getFaction();
        List<String> rules = faction.getRules();

        if (args.size() == 0) {
            if (rules.size() == 0) {
                configuration.defaultRules.forEach(rule ->
                        fme.sendMessage(ChatColor.translateAlternateColorCodes('&', rule)));
                return;
            }

            for (String rule : rules) {
                fme.sendMessage(ChatColor.translateAlternateColorCodes('&', rule));
            }

            return;
        }

        if (args.size() == 1) {
            TL error = TL.COMMAND_RULES_NO_SUB_COMMAND;

            switch (args.get(0).toLowerCase()) {
                case "add":
                    error = TL.COMMAND_RULES_ADD_INVALIDARGS;
                    break;
                case "set":
                    error = TL.COMMAND_RULES_SET_INVALIDARGS;
                    break;
                case "remove":
                    error = TL.COMMAND_RULES_REMOVE_INVALIDARGS;
                    break;
                case "clear":
                    fme.msg(TL.COMMAND_RULES_CLEAR_SUCCESS);
                    faction.clearRules();
                    return;
            }

            fme.msg(error, args.get(0));
        }

        if (args.size() >= 2) {
            if (args.get(0).equalsIgnoreCase("add")) {
                String message = "";
                StringBuilder string = new StringBuilder(message);
                for (int i = 1; i <= args.size() - 1; i++) {
                    string.append(" " + args.get(i));
                }
                faction.addRule(string.toString());
                fme.msg(TL.COMMAND_RULES_ADD_SUCCESS);
            }

            if (args.size() != 2 || !args.get(0).equalsIgnoreCase("remove")) {
                return;
            }

            int index = argAsInt(1);

            boolean result = faction.removeRule(index);

            if (result) {
                fme.msg(TL.COMMAND_RULES_REMOVE_SUCCESS);
                return;
            }

            if (faction.getRules().size() == 0) {
                fme.msg(TL.COMMAND_RULES_EMPTY);
                return;
            }

            fme.msg(TL.COMMAND_RULES_REMOVE_INVALID_INDEX, faction.getRules().size());
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_RULES_DESCRIPTION;
    }
}
