package com.massivecraft.factions.struct;

import com.massivecraft.factions.*;
import com.massivecraft.factions.util.Particles.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class WarBanner implements IWarBanner {
    private final Faction faction;
    private final Location location;
    private final World world;

    private final int effectRadius;
    private final List<PotionEffect> effects;

    private ArmorStand armorStand;
    private int age;

    public WarBanner(Faction faction, Location location) {
        this.faction = faction;
        this.location = location;
        this.world = location.getWorld();

        this.effectRadius = SavageFactionsPlugin.plugin.getConfig().getInt("fbanners.Banner-Effect-Radius");
        this.effects = new ArrayList<>();

        for (String effectName : SavageFactionsPlugin.plugin.getConfig().getStringList("fbanners.Effects")) {
            String[] components = effectName.split(":");

            PotionEffectType effectType = PotionEffectType.getByName(components[0]);
            int duration = Integer.parseInt(components[1]);
            effects.add(new PotionEffect(effectType, 100, duration));
        }
    }

    @Override
    public void spawn() {
        world.strikeLightningEffect(location);

        this.armorStand = (ArmorStand) world.spawnEntity(location.clone().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        armorStand.setCustomName(SavageFactionsPlugin.plugin.color(SavageFactionsPlugin.plugin.getConfig().getString("fbanners.BannerHolo").replace("{Faction}", faction.getTag())));
        armorStand.setCustomNameVisible(true);
    }

    @Override
    public void remove() {
        armorStand.remove();
        location.getBlock().setType(Material.AIR);
        world.strikeLightningEffect(location);
    }

    @Override
    public void update() {
        age++;

        for (Entity entity : world.getNearbyEntities(location, effectRadius, effectRadius, effectRadius)) {
            if (!(entity instanceof Player)) {
                continue;
            }

            Player player = (Player) entity;
            FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);

            if (!fplayer.getFaction().getUniqueId().equals(faction.getUniqueId())) {
                continue;
            }

            for (PotionEffect effect : effects) {
                player.addPotionEffect(effect);
            }

            ParticleEffect.LAVA.display(1, 1, 1, 0.5F, 6, location, 4);
            ParticleEffect.FLAME.display(1, 1, 1, 0.5F, 6, location, 4);
        }
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public long getAge() {
        return age;
    }
}
