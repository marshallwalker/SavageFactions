package com.massivecraft.factions.struct;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.ServerVersion;
import com.massivecraft.factions.fperms.Permissable;
import com.massivecraft.factions.permissable.PermissableGroup;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public enum Role implements Permissable, PermissableGroup {
	LEADER(4, TL.ROLE_LEADER),
	COLEADER(3, TL.ROLE_COLEADER),
	MODERATOR(2, TL.ROLE_MODERATOR),
	NORMAL(1, TL.ROLE_NORMAL),
	RECRUIT(0, TL.ROLE_RECRUIT);

	public final int value;
	public final String nicename;
	public final TL translation;


	Role(final int value, final TL translation) {
		this.value = value;
		this.nicename = translation.toString();
		this.translation = translation;
	}

	public static Role getRelative(Role role, int relative) {
		return Role.getByValue(role.value + relative);
	}

	public static Role getByValue(int value) {
		switch (value) {
			case 0:
				return RECRUIT;
			case 1:
				return NORMAL;
			case 2:
				return MODERATOR;
			case 3:
				return COLEADER;
			case 4:
				return LEADER;
		}

		return null;
	}

	public static Role fromString(String check) {
		switch (check.toLowerCase()) {
			case "leader":
			case "admin":
				return LEADER;
			case "coleader":
				return COLEADER;
			case "mod":
			case "moderator":
				return MODERATOR;
			case "normal":
			case "member":
				return NORMAL;
			case "recruit":
			case "rec":
				return RECRUIT;
		}

		return null;
	}

	public boolean isAtLeast(Role role) {
		return this.value >= role.value;
	}

	public boolean isAtMost(Role role) {
		return this.value <= role.value;
	}

	@Override
	public String toString() {
		return this.nicename;
	}

	public TL getTranslation() {
		return translation;
	}

	public String getPrefix() {

		switch (this) {
			case LEADER:
				return Conf.prefixLeader;
			case COLEADER:
				return Conf.prefixCoLeader;
			case MODERATOR:
				return Conf.prefixMod;
			case NORMAL:
				return Conf.prefixNormal;
			case RECRUIT:
				return Conf.prefixRecruit;
		}

		return "";
	}

	// Utility method to buildButton items for F Perm GUI
	@Override
	public ItemStack buildItem() {
		final ConfigurationSection RELATION_CONFIG = SavageFactionsPlugin.plugin.getConfig().getConfigurationSection("fperm-gui.relation");

		String displayName = replacePlaceholders(RELATION_CONFIG.getString("placeholder-item.name", ""));
		List<String> lore = new ArrayList<>();

		Material material = Material.matchMaterial(RELATION_CONFIG.getString("materials." + name().toLowerCase(), "STAINED_CLAY"));
		if (material == null) {
			return null;
		}

		ItemStack item = new ItemStack(material);
		ItemMeta itemMeta = item.getItemMeta();

		for (String loreLine : RELATION_CONFIG.getStringList("placeholder-item.lore")) {
			lore.add(replacePlaceholders(loreLine));
		}

		itemMeta.setDisplayName(displayName);
		itemMeta.setLore(lore);

		if (SavageFactionsPlugin.plugin.serverVersion != ServerVersion.MC_V17) {
			itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}

		item.setItemMeta(itemMeta);

		return item;
	}

	public String replacePlaceholders(String string) {
		string = ChatColor.translateAlternateColorCodes('&', string);

		String permissableName = nicename.substring(0, 1).toUpperCase() + nicename.substring(1);

		string = string.replace("{relation-color}", ChatColor.GREEN.toString());
		string = string.replace("{relation}", permissableName);

		return string;
	}
}