package com.massivecraft.factions.zcore.util;

import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.ServerVersion;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BannerFactory {
    private static BannerFactory instance;

    private BannerFactory() {
    }

    private String formatPattern(Pattern pattern) {
        return pattern.getPattern().getIdentifier() + ":" + pattern.getColor().name();
    }

    public ItemStack fromPattern(String pattern) {
        if(SavageFactionsPlugin.plugin.serverVersion == ServerVersion.MC_V17 || pattern == null || pattern.isEmpty()) return null;

        ItemStack itemStack = new ItemStack(SavageFactionsPlugin.plugin.BANNER);
        BannerMeta bannerMeta = (BannerMeta) itemStack.getItemMeta();
        List<Pattern> patterns = new ArrayList<>();

        if(SavageFactionsPlugin.plugin.serverVersion == ServerVersion.MC_V18) {
            String[] split = pattern.split(";");
            bannerMeta.setBaseColor(DyeColor.valueOf(split[0]));

            if(split.length == 2) {
                patterns = Arrays.stream(split[1].split(",")).map(patternString -> {
                    String[] parts = patternString.split(":");
                    PatternType patternType = PatternType.getByIdentifier(parts[0]);
                    DyeColor dyeColor = DyeColor.valueOf(parts[1]);

                    return new Pattern(dyeColor, patternType);
                }).collect(Collectors.toList());
            }
        }

        bannerMeta.setPatterns(patterns);
        itemStack.setItemMeta(bannerMeta);
        return itemStack;
    }

    public String toPattern(ItemStack itemStack) {
        if(SavageFactionsPlugin.plugin.serverVersion == ServerVersion.MC_V17) return null;

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (!(itemMeta instanceof BannerMeta)) {
            return null;
        }

        BannerMeta bannerMeta = (BannerMeta) itemMeta;
        String pattern = "";

        if (SavageFactionsPlugin.plugin.serverVersion == ServerVersion.MC_V18) {
            pattern = bannerMeta.getBaseColor().name() + ";";
        }

        return pattern + bannerMeta.getPatterns().stream()
                .map(this::formatPattern)
                .collect(Collectors.joining(","));
    }

    public static BannerFactory getInstance() {
        return instance == null ? (instance = new BannerFactory()) : instance;
    }
}
