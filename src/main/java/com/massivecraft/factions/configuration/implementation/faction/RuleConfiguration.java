package com.massivecraft.factions.configuration.implementation.faction;

import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class RuleConfiguration {
    public boolean enabled = true;

    public List<String> defaultRules = Lists.newArrayList(
            "&cNo faction rules are set.",
            "&cUse /f rule add");
}
