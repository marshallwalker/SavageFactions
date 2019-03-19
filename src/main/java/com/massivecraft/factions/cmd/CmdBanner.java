package com.massivecraft.factions.cmd;

import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CmdBanner extends FCommand {
	public CmdBanner() {
		super();

		this.aliases.add("banner");
		this.aliases.add("warbanner");

		this.permission = Permission.BANNER.node;
		this.disableOnLock = false;


		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeModerator = false;
		senderMustBeColeader = true;
		senderMustBeAdmin = false;
	}

	@Override
	public void perform() {
		if (!SavageFactionsPlugin.plugin.getConfig().getBoolean("fbanners.Enabled")) {
			msg(TL.COMMAND_BANNER_DISABLED);
			return;
		}

		double cost = SavageFactionsPlugin.plugin.getConfig().getInt("fbanners.Banner-Cost", 5000);

		if (!fme.canAfford(cost)) {
			msg(TL.COMMAND_BANNER_NOTENOUGHMONEY);
			return;
		}

		fme.sendMessage(TL.COMMAND_BANNER_MONEYTAKE.toString().replace("{amount}", cost + ""));
		fme.withdraw(cost);

		//ItemStack warBanner = SavageFactionsPlugin.plugin.createItem(Material.BANNER, 1, (short) 1, SavageFactionsPlugin.plugin.getConfig().getString("fbanners.Item.Name"), SavageFactionsPlugin.plugin.getConfig().getStringList("fbanners.Item.Lore"));
		//BannerMeta bannerMeta = (BannerMeta) warBanner.getItemMeta();
		ItemStack warBanner = fme.getFaction().getBanner();
		if (warBanner != null) {
			ItemMeta warmeta = warBanner.getItemMeta();
			warmeta.setDisplayName(SavageFactionsPlugin.plugin.color(SavageFactionsPlugin.plugin.getConfig().getString("fbanners.Item.Name")));
			warmeta.setLore(SavageFactionsPlugin.plugin.colorList(SavageFactionsPlugin.plugin.getConfig().getStringList("fbanners.Item.Lore")));
			warBanner.setItemMeta(warmeta);


		} else {


			warBanner = SavageFactionsPlugin.plugin.createItem(SavageFactionsPlugin.plugin.BANNER, 1, (short) 1, SavageFactionsPlugin.plugin.getConfig().getString("fbanners.Item.Name"), SavageFactionsPlugin.plugin.getConfig().getStringList("fbanners.Item.Lore"));
		}
		fme.msg(TL.COMMAND_BANNER_SUCCESS);
		warBanner.setAmount(1);
		me.getInventory().addItem(warBanner);
	}

	@Override
	public TL getUsageTranslation() {
		return TL.COMMAND_BANNER_DESCRIPTION;
	}
}
