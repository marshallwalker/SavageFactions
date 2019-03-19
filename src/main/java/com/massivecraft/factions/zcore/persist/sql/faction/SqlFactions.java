package com.massivecraft.factions.zcore.persist.sql.faction;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.SavageFactionsPlugin;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.fperms.Permissable;
import com.massivecraft.factions.zcore.persist.MemoryFactions;
import com.massivecraft.factions.zcore.persist.sql.SqlBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SqlFactions extends MemoryFactions {
    private static final SqlBuilder sqlBuilder = SqlBuilder.getInstance();

    public SqlFactions() {
        sqlBuilder.path("faction/create_table").execute();
        sqlBuilder.path("faction/rule/create_table").execute();
        sqlBuilder.path("faction/upgrade/create_table").execute();
        sqlBuilder.path("faction/warp/create_table").execute();
        sqlBuilder.path("faction/ban/create_table").execute();
        sqlBuilder.path("faction/relation/create_table").execute();
        sqlBuilder.path("faction/invite/create_table").execute();
        sqlBuilder.path("faction/claim_ownership/create_table").execute();
        sqlBuilder.path("faction/announcement/create_table").execute();
        sqlBuilder.path("faction/home/create_table").execute();

        List<Permissable> permissables = new ArrayList<>();
        permissables.addAll(Arrays.asList(Role.values()));
        permissables.addAll(Arrays.asList(Relation.values()));

        permissables.forEach(permissable -> sqlBuilder
                .path("faction/permission/create_table", permissable.name().toLowerCase())
                .executeAsyncBatched());
    }

    @Override
    public void load() {
        SavageFactionsPlugin.plugin.log("Loading factions from database...");

        sqlBuilder.path("faction/select_all").queryEach(result -> {
            UUID factionId = UUID.fromString(result.getString(1));
            factions.put(factionId, new SqlFaction(factionId, result));
        });

        SavageFactionsPlugin.plugin.log("Loaded " + factions.size() + " factions!");
        super.load();
    }

    @Override
    public void removeFactionById(UUID uniqueId) {
        super.removeFactionById(uniqueId);

        sqlBuilder.path("remove_by", "faction", "faction_id").args(uniqueId).executeAsyncBatched();
        sqlBuilder.path("remove_by", "faction_upgrade", "faction_id").args(uniqueId).executeAsyncBatched();
        sqlBuilder.path("remove_by", "faction_rule", "faction_id").args(uniqueId).executeAsyncBatched();
        sqlBuilder.path("remove_by", "faction_warp", "faction_id").args(uniqueId).executeAsyncBatched();
        sqlBuilder.path("remove_by", "faction_ban", "faction_id").args(uniqueId).executeAsyncBatched();
        sqlBuilder.path("remove_by", "faction_relation", "faction_id").args(uniqueId).executeAsyncBatched();
        sqlBuilder.path("remove_by", "faction_invite", "faction_id").args(uniqueId).executeAsyncBatched();
        sqlBuilder.path("remove_by", "faction_claim_ownership", "faction_id").args(uniqueId).executeAsyncBatched();
        sqlBuilder.path("remove_by", "faction_announcement", "faction_id").args(uniqueId).executeAsyncBatched();
        sqlBuilder.path("remove_by", "faction_home", "faction_id").args(uniqueId).executeAsyncBatched();
    }

    @Override
    public Faction createFaction(String tag) {
        Faction faction = super.createFaction(tag);

        sqlBuilder.path("faction/insert_faction")
                .args(faction.getUniqueId(),
                        tag,
                        System.currentTimeMillis(),
                        Role.RECRUIT.name())
                .executeAsync();

        return faction;
    }

    @Override
    public Faction generateFaction(String tag) {
        return new SqlFaction(UUID.randomUUID(), tag);
    }

    @Override
    public Faction generateFaction(UUID uniqueId, String tag) {
        return new SqlFaction(uniqueId, tag);
    }

    @Override
    public void convertFrom(MemoryFactions old) {
        //do nothing
    }

    @Override
    public void forceSave() {
        //do nothing
    }

    @Override
    public void forceSave(boolean sync) {
        //do nothing
    }
}
