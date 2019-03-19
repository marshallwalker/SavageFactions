package com.massivecraft.factions.zcore.persist.sql.faction;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.BanInfo;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.LazyLocation;
import com.massivecraft.factions.struct.Access;
import com.massivecraft.factions.fperms.Permissable;
import com.massivecraft.factions.struct.PermissableAction;
import com.massivecraft.factions.struct.Upgrade;
import com.massivecraft.factions.zcore.persist.MemoryFaction;
import com.massivecraft.factions.zcore.persist.sql.SqlBuilder;
import com.massivecraft.factions.zcore.persist.sql.warp.SqlFWarp;
import com.massivecraft.factions.zcore.util.BannerFactory;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class SqlFaction extends MemoryFaction {
    private static final SqlBuilder sqlBuilder = SqlBuilder.getInstance();
    private static final BannerFactory bannerFactory = BannerFactory.getInstance();

    public SqlFaction(UUID id, String tag) {
        super(id);

        this.tag = tag;
    }

    public SqlFaction(UUID uniqueId, ResultSet factionResult) throws SQLException {
        this.uniqueId = uniqueId;
        this.tag = factionResult.getString(2);

        sqlBuilder.path("faction/rule/select_by_faction_id").args(uniqueId).queryEach(result ->
                rules.add(result.getString(1)));

        this.tnt = factionResult.getInt(3);

        sqlBuilder.path("faction/upgrade/select_by_faction_id").args(uniqueId).queryEach(result ->
                upgrades.put(result.getString(1), result.getInt(2)));

        this.peacefulExplosionsEnabled = factionResult.getBoolean(4);
        this.permanent = factionResult.getBoolean(5);
        this.description = factionResult.getString(6);
        this.open = factionResult.getBoolean(7);
        this.peaceful = factionResult.getBoolean(8);
        this.foundedDate = factionResult.getLong(9);
        this.powerBoost = factionResult.getDouble(10);

        sqlBuilder.path("faction/claim_ownership/select_by_faction_id").args(uniqueId).queryEach(result -> {
            UUID playerId = UUID.fromString(result.getString(1));
            String worldName = result.getString(2);
            int chunkX = result.getInt(3);
            int chunkZ = result.getInt(4);

            FLocation fLocation = new FLocation(worldName, chunkX, chunkZ);
            Set<UUID> owners= claimOwnership.computeIfAbsent(fLocation, f -> new HashSet<>());
            owners.add(playerId);
        });

        sqlBuilder.path("faction/relation/select_by_faction_id").args(uniqueId).queryEach(result -> {
            UUID targetId = UUID.fromString(result.getString(1));
            Relation relation = Relation.fromString(result.getString(2));
            relationWish.put(targetId, relation);
        });

        sqlBuilder.path("faction/invite/select_by_faction_id").args(uniqueId).queryEach(result ->
                invites.add(UUID.fromString(result.getString(1))));

        sqlBuilder.path("faction/announcement/select_by_faction_id").args(uniqueId).queryEach(result -> {
            UUID playerId = UUID.fromString(result.getString(1));
            List<String> messages = announcements.computeIfAbsent(playerId, f -> new ArrayList<>());
            messages.add(result.getString(2));
        });

        sqlBuilder.path("faction/warp/select_by_faction_id").args(uniqueId).queryEach(result -> {
            String warpName = result.getString(1);
            this.warps.put(warpName, new SqlFWarp(uniqueId, result));
        });

        this.maxVaults = factionResult.getInt(11);

        sqlBuilder.path("faction/ban/select_by_faction_id").args(uniqueId).queryEach(result ->
                bans.add(new BanInfo(result)));

        this.defaultRole = Role.valueOf(factionResult.getString(12));

        List<Permissable> permissables = new ArrayList<>();
        permissables.addAll(Arrays.asList(Relation.values()));
        permissables.addAll(Arrays.asList(Role.values()));
        permissables.forEach(permissable -> sqlBuilder
                .path("faction/permission/select_by_faction_id", permissable.name().toLowerCase())
                .args(uniqueId)
                .query(relationResult -> processPermissions(permissable, relationResult)));

        this.lastDeath = factionResult.getLong(13);

        sqlBuilder.path("faction/home/select_by_faction_id").args(uniqueId).queryEach(result ->
                this.home = new LazyLocation(result));

        this.paypal = factionResult.getString(14);
        this.banner = bannerFactory.fromPattern(factionResult.getString(15));
    }

    private void processPermissions(Permissable permissable, ResultSet result) throws SQLException {
        ResultSetMetaData metaData = result.getMetaData();
        Map<PermissableAction, Access> map = new HashMap<>();

        while (result.next()) {
            int size = metaData.getColumnCount();

            for (int i = 1; i < size; i++) {
                String fieldName = metaData.getColumnName(i);
                PermissableAction action = PermissableAction.fromString(fieldName);
                Access access = Access.fromString(result.getString(fieldName));
                map.put(action, access);
            }
        }

        permissions.put(permissable, map);
        result.close();
    }

    @Override
    public void resetPerms() {
        if (!isNormal()) {
            return;
        }

        super.resetPerms();

        permissions.forEach((key, value) -> {
            List<String> list = new ArrayList<>();
            list.add(uniqueId.toString());
            value.forEach((action, access) -> list.add(access.name()));

            sqlBuilder
                    .path("faction/permission/insert", key.name().toLowerCase())
                    .args(list.toArray())
                    .executeAsyncBatched();
        });
    }

    @Override
    public void setPermission(Permissable permissable, PermissableAction permissableAction, Access access) {
        super.setPermission(permissable, permissableAction, access);

        sqlBuilder
                .path("faction/permission/set_field", permissable.name().toLowerCase(), permissableAction.name())
                .args(access.name(),
                        uniqueId)
                .executeAsyncBatched();
    }

    @Override
    public void addAnnouncement(FPlayer fPlayer, String msg) {
        super.addAnnouncement(fPlayer, msg);

        sqlBuilder
                .path("faction/announcement/insert")
                .args(uniqueId,
                        fPlayer.getId(),
                        msg)
                .executeAsync();
    }

    @Override
    public void removeAnnouncements(FPlayer fPlayer) {
        super.removeAnnouncements(fPlayer);

        sqlBuilder.path("faction/announcement/remove_by_player_id")
                .args(fPlayer.getId(),
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void sendUnreadAnnouncements(FPlayer fPlayer) {
        if (announcements.containsKey(fPlayer.getId())) {
            sqlBuilder.path("faction/announcement/remove_by_player_id")
                    .args(fPlayer.getId(),
                            uniqueId)
                    .executeAsync();
        }

        super.sendUnreadAnnouncements(fPlayer);
    }

    @Override
    public void setPlayerAsOwner(FPlayer player, FLocation loc) {
        super.setPlayerAsOwner(player, loc);

        sqlBuilder.path("faction/claim_ownership/insert")
                .args(uniqueId,
                        player.getId(),
                        loc.getWorldName(),
                        loc.getX(),
                        loc.getX())
                .executeAsync();
    }

    @Override
    public void removePlayerAsOwner(FPlayer player, FLocation loc) {
        super.removePlayerAsOwner(player, loc);

//        sqlBuilder.path("faction/claim_ownership/remove_by_id")
//                .args(player.getId(),
//                        uniqueId,
//                        loc.getWorldName(),
//                        loc.getX(),
//                        loc.getZ())
//                .executeAsync();
    }

    @Override
    public void clearAllClaimOwnership() {
        super.clearAllClaimOwnership();

//        sqlBuilder.path("faction/claim_ownership/remove_by_faction_id")
//                .args(uniqueId)
//                .executeAsync();
    }

    @Override
    public void invite(FPlayer fplayer) {
        super.invite(fplayer);

        sqlBuilder.path("faction/invite/insert")
                .args(uniqueId,
                        fplayer.getId())
                .executeAsync();
    }

    @Override
    public void deinvite(FPlayer fplayer) {
        super.deinvite(fplayer);

        sqlBuilder.path("faction/invite/remove_by_player_id")
                .args(fplayer.getId(),
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void setRelationWish(Faction otherFaction, Relation relation) {
        super.setRelationWish(otherFaction, relation);

        sqlBuilder.path("faction/relation/insert")
                .args(uniqueId,
                        otherFaction.getUniqueId(),
                        relation.name())
                .executeAsync();
    }

    @Override
    public void setTag(String str) {
        super.setTag(str);

        sqlBuilder.path("faction/set_field", "tag")
                .args(str,
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void addRule(String rule) {
        super.addRule(rule);

        sqlBuilder.path("faction/rule/insert")
                .args(uniqueId,
                        rules.size() - 1,
                        rule)
                .executeAsync();
    }

    @Override
    public void removeRule(int index) {
        if(rules.get(index) == null) {
            return;
        }

        super.removeRule(index);

        sqlBuilder.path("faction/rule/remove_by_faction_id")
                .args(uniqueId)
                .executeAsync();

        for (String rule : rules) {
            sqlBuilder.path("faction/rule/insert")
                    .args(uniqueId,
                            rules.size() - 1,
                            rule)
                    .executeAsync();
        }
    }

    @Override
    public void addTnt(int amt) {
        super.addTnt(amt);

        sqlBuilder.path("faction/set_field", "tnt")
                .args(tnt,
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void setTnt(int tnt) {
        super.setTnt(tnt);

        sqlBuilder.path("faction/set_field", "tnt")
                .args(tnt,
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void takeTnt(int amt) {
        super.takeTnt(amt);

        sqlBuilder.path("faction/set_field", "tnt")
                .args(tnt,
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void setUpgrade(Upgrade upgrade, int level) {
        super.setUpgrade(upgrade, level);

        sqlBuilder.path("faction/upgrade/insert")
                .args(uniqueId,
                        upgrade.toString(),
                        level)
                .executeAsync();
    }

    @Override
    public void setPeacefulExplosionsEnabled(boolean val) {
        super.setPeacefulExplosionsEnabled(val);

        sqlBuilder.path("faction/set_field", "set_peaceful_explosion")
                .args(val,
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void setPermanent(boolean isPermanent) {
        super.setPermanent(isPermanent);

        sqlBuilder.path("faction/set_field", "permanent")
                .args(isPermanent,
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void setDescription(String value) {
        super.setDescription(value);

        sqlBuilder.path("faction/set_field", "description")
                .args(value,
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void setOpen(boolean isOpen) {
        super.setOpen(isOpen);

        sqlBuilder.path("faction/set_field", "open")
                .args(isOpen,
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void setPeaceful(boolean isPeaceful) {
        super.setPeaceful(isPeaceful);

        sqlBuilder.path("faction/set_field", "peaceful")
                .args(isPeaceful,
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void setFoundedDate(long newDate) {
        super.setFoundedDate(newDate);

        sqlBuilder.path("faction/set_field", "founded_date")
                .args(newDate,
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void setPowerBoost(double powerBoost) {
        super.setPowerBoost(powerBoost);

        sqlBuilder.path("faction/set_field", "power_boost")
                .args(powerBoost,
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void setWarp(String name, String password, LazyLocation loc) {
        warps.put(name, new SqlFWarp(uniqueId, name, password, loc));

        sqlBuilder.path("faction/warp/insert")
                .args(uniqueId,
                        name,
                        password,
                        loc.getWorldName(),
                        loc.getX(),
                        loc.getY(),
                        loc.getZ(),
                        loc.getYaw(),
                        loc.getPitch())
                .executeAsync();
    }

    @Override
    public boolean removeWarp(String name) {
        sqlBuilder.path("faction/warp/remove_by_warp_name")
                .args(name,
                        uniqueId)
                .executeAsync();

        return super.removeWarp(name);
    }

    @Override
    public void clearWarps() {
        super.clearWarps();

        sqlBuilder.path("faction/warp/remove_by_faction_id")
                .args(uniqueId)
                .executeAsync();
    }

    @Override
    public void ban(FPlayer target, FPlayer banner) {
        super.ban(target, banner);

        sqlBuilder.path("faction/ban/insert")
                .args(uniqueId,
                        banner.getId(),
                        target.getId(),
                        System.currentTimeMillis())
                .executeAsync();
    }

    @Override
    public void unban(FPlayer player) {
        super.unban(player);

        sqlBuilder.path("faction/ban/insert")
                .args(player.getId(),
                        uniqueId)
                .executeAsync();
    }

    @Override
    public void setHome(Location home) {
        super.setHome(home);

        sqlBuilder.path("faction/home/insert")
                .args(uniqueId,
                        home.getWorld().getName(),
                        home.getX(),
                        home.getY(),
                        home.getZ(),
                        home.getYaw(),
                        home.getPitch())
                .executeAsync();
    }

    @Override
    public void setBannerPattern(ItemStack banner) {
        super.setBannerPattern(banner);

        sqlBuilder.path("faction/set_field", "banner_pattern")
                .args(bannerFactory.toPattern(banner),
                        uniqueId)
                .executeAsync();
    }
}
