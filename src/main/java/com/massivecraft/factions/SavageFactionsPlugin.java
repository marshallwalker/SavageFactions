package com.massivecraft.factions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import com.earth2me.essentials.Essentials;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.cmd.CmdAutoHelp;
import com.massivecraft.factions.cmd.FCmdRoot;
import com.massivecraft.factions.configuration.Configuration;
import com.massivecraft.factions.configuration.ConfigurationBuilder;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.WorldGuard;
import com.massivecraft.factions.integration.dynmap.EngineDynmap;
import com.massivecraft.factions.integration.placeholder.ClipPlaceholderAPIManager;
import com.massivecraft.factions.listeners.*;
import com.massivecraft.factions.struct.Access;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.PermissableAction;
import com.massivecraft.factions.util.*;
import com.massivecraft.factions.zcore.CommandVisibility;
import com.massivecraft.factions.zcore.MCommand;
import com.massivecraft.factions.zcore.MPlugin;
import com.massivecraft.factions.fperms.Permissable;
import com.massivecraft.factions.zcore.persist.sql.SqlBuilder;
import com.massivecraft.factions.zcore.util.TextUtil;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class SavageFactionsPlugin extends MPlugin {
    // Our singleton plugin instance.
    public static SavageFactionsPlugin plugin;

    @Getter private Configuration configuration;
    @Getter private File configurationFile;

    public static Permission perms = null;
    // This plugin sets the boolean true when fully enabled.
    // Plugins can check this boolean while hooking in have
    // a green light to use the api.
    public static boolean startupFinished = false;

    // Persistence related
    public static ArrayList<FPlayer> playersFlying = new ArrayList();
    public Essentials ess;
    public boolean PlaceholderApi;
    // Commands
    public FCmdRoot cmdBase;
    public CmdAutoHelp cmdAutoHelp;

    public ServerVersion serverVersion;

    public boolean useNonPacketParticles = false;
    public boolean factionsFlight = false;
    //multiversion material fields

    @Deprecated
    public Material SUGAR_CANE_BLOCK, BANNER, CROPS, REDSTONE_LAMP_ON,
            STAINED_GLASS, STATIONARY_WATER, STAINED_CLAY, WOOD_BUTTON,
            SOIL, MOB_SPANWER, THIN_GLASS, IRON_FENCE, NETHER_FENCE, FENCE,
            WOODEN_DOOR, TRAP_DOOR, FENCE_GATE, BURNING_FURNACE, DIODE_BLOCK_OFF,
            DIODE_BLOCK_ON, ENCHANTMENT_TABLE, FIREBALL;
    SkriptAddon skriptAddon;
    private boolean locked = false;
    private Integer AutoLeaveTask = null;
    private ClipPlaceholderAPIManager clipPlaceholderAPIManager;
    private boolean mvdwPlaceholderAPIManager = false;

    public SavageFactionsPlugin() {
        plugin = this;
    }

    public boolean getLocked() {
        return this.locked;
    }

    public void setLocked(boolean val) {
        this.locked = val;
        this.setAutoSave(val);
    }

    @Override
    public void onEnable() {
        ConfigurationBuilder configurationBuilder = ConfigurationBuilder.getInstance();
        this.configurationFile = new File(getDataFolder(), "configuration.json");

        try {
            this.configuration = configurationBuilder
                    .from(configurationFile)
                    .to(Configuration.class);

            if(!configurationFile.exists()) {
                configurationFile.createNewFile();

                configurationBuilder
                        .from(configuration)
                        .to(configurationFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            getServer().shutdown();
        }

        log("==== Setup ====");

        // Vault dependency check.
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            log("Vault is not present, the plugin will not run properly.");
            getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        serverVersion = ServerVersion.getVersion();

        if(serverVersion == ServerVersion.MC_V1_13) {
            changeItemIDSInConfig();
        }

        setupMultiversionMaterials();
        log("==== End Setup ====");

        if (!preEnable()) {
            return;
        }
        this.loadSuccessful = false;

        saveDefaultConfig();

        // Load Conf from disk
        Conf.load();
        com.massivecraft.factions.integration.Essentials.setup();

        if (Conf.backEnd == Conf.Backend.SQL) {
            SqlBuilder sqlBuilder = SqlBuilder.getInstance();
            sqlBuilder.path("create_database").execute();
        }

        FPlayers.getInstance().load();
        Factions.getInstance().load();

        for (FPlayer fPlayer : FPlayers.getInstance().getAllFPlayers()) {
            Faction faction = Factions.getInstance().getFactionById(fPlayer.getFactionId());
            if (faction == null) {
                log("Invalid faction uniqueId on " + fPlayer.getName() + ":" + fPlayer.getFactionId());
                fPlayer.resetFactionData(false);
                continue;
            }
            faction.addFPlayer(fPlayer);
        }
        playersFlying.clear();
        playersFlying.addAll(FPlayers.getInstance().getAllFPlayers());
        UtilFly.run();

        Board.getInstance().load();
        Board.getInstance().clean();

        // Add Base Commands
        this.cmdBase = new FCmdRoot();
        this.cmdAutoHelp = new CmdAutoHelp();
        this.getBaseCommands().add(cmdBase);

        Econ.setup();
        setupPermissions();

        if (Conf.worldGuardChecking || Conf.worldGuardBuildPriority) {
            WorldGuard.init(this);
        }

        EngineDynmap.getInstance().init();

        // start up task which runs the autoLeaveAfterDaysOfInactivity routine
        startAutoLeaveTask(false);

        if(serverVersion != ServerVersion.MC_V17 && serverVersion != ServerVersion.MC_V18) {
            log("Minecraft Version 1.9 or higher found, using non packet based particle API");
            useNonPacketParticles = true;
        }

        if (getConfig().getBoolean("enable-faction-flight")) {
            factionsFlight = true;
        }

        if (getServer().getPluginManager().getPlugin("Skript") != null) {
            log("Skript was found! Registering SavageFactionsPlugin Addon...");
            skriptAddon = Skript.registerAddon(this);
            try {
                skriptAddon.loadClasses("com.massivecraft.factions.skript", "expressions");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            log("Skript addon registered!");
        }


        // Register Event Handlers
        Listener[] eventsListener = new Listener[]{
                new InventoryListener(),
                new FactionsPlayerListener(),
                new FactionsChatListener(),
                new FactionsEntityListener(this),
                new FactionsExploitListener(),
                new FactionsBlockListener(this)
        };

        for (Listener eventListener : eventsListener)
            getServer().getPluginManager().registerEvents(eventListener, this);

        // since some other plugins execute commands directly through this command interface, provide it
        PluginCommand command = getCommand(refCommand);
        command.setExecutor(this);
        command.setTabCompleter(this);

        setupEssentials();

        this.setupPlaceholderAPI();
        this.postEnable();
        this.loadSuccessful = true;
        // Set startup finished to true. to give plugins hooking in a greenlight
        SavageFactionsPlugin.startupFinished = true;
    }

    public SkriptAddon getSkriptAddon() {
        return skriptAddon;
    }

    private void setupMultiversionMaterials() {
        if (serverVersion == ServerVersion.MC_V1_13) {
            BANNER = Material.valueOf("LEGACY_BANNER");
            CROPS = Material.valueOf("LEGACY_CROPS");
            SUGAR_CANE_BLOCK = Material.valueOf("LEGACY_SUGAR_CANE_BLOCK");
            REDSTONE_LAMP_ON = Material.valueOf("LEGACY_REDSTONE_LAMP_ON");
            STAINED_GLASS = Material.valueOf("LEGACY_STAINED_GLASS");
            STATIONARY_WATER = Material.valueOf("LEGACY_STATIONARY_WATER");
            STAINED_CLAY = Material.valueOf("LEGACY_STAINED_CLAY");
            WOOD_BUTTON = Material.valueOf("LEGACY_WOOD_BUTTON");
            SOIL = Material.valueOf("LEGACY_SOIL");
            MOB_SPANWER = Material.valueOf("LEGACY_MOB_SPAWNER");
            THIN_GLASS = Material.valueOf("LEGACY_THIN_GLASS");
            IRON_FENCE = Material.valueOf("LEGACY_IRON_FENCE");
            NETHER_FENCE = Material.valueOf("LEGACY_NETHER_FENCE");
            FENCE = Material.valueOf("LEGACY_FENCE");
            WOODEN_DOOR = Material.valueOf("LEGACY_WOODEN_DOOR");
            TRAP_DOOR = Material.valueOf("LEGACY_TRAP_DOOR");
            FENCE_GATE = Material.valueOf("LEGACY_FENCE_GATE");
            BURNING_FURNACE = Material.valueOf("LEGACY_BURNING_FURNACE");
            DIODE_BLOCK_OFF = Material.valueOf("LEGACY_DIODE_BLOCK_OFF");
            DIODE_BLOCK_ON = Material.valueOf("LEGACY_DIODE_BLOCK_ON");
            ENCHANTMENT_TABLE = Material.valueOf("LEGACY_ENCHANTMENT_TABLE");
            FIREBALL = Material.valueOf("LEGACY_FIREBALL");

        } else {
            if (serverVersion != ServerVersion.MC_V17) {
                BANNER = Material.valueOf("BANNER");
            }
            CROPS = Material.valueOf("CROPS");
            SUGAR_CANE_BLOCK = Material.valueOf("SUGAR_CANE_BLOCK");
            REDSTONE_LAMP_ON = Material.valueOf("REDSTONE_LAMP_ON");
            STAINED_GLASS = Material.valueOf("STAINED_GLASS");
            STATIONARY_WATER = Material.valueOf("STATIONARY_WATER");
            STAINED_CLAY = Material.valueOf("STAINED_CLAY");
            WOOD_BUTTON = Material.valueOf("WOOD_BUTTON");
            SOIL = Material.valueOf("SOIL");
            MOB_SPANWER = Material.valueOf("MOB_SPAWNER");
            THIN_GLASS = Material.valueOf("THIN_GLASS");
            IRON_FENCE = Material.valueOf("IRON_FENCE");
            NETHER_FENCE = Material.valueOf("NETHER_FENCE");
            FENCE = Material.valueOf("FENCE");
            WOODEN_DOOR = Material.valueOf("WOODEN_DOOR");
            TRAP_DOOR = Material.valueOf("TRAP_DOOR");
            FENCE_GATE = Material.valueOf("FENCE_GATE");
            BURNING_FURNACE = Material.valueOf("BURNING_FURNACE");
            DIODE_BLOCK_OFF = Material.valueOf("DIODE_BLOCK_OFF");
            DIODE_BLOCK_ON = Material.valueOf("DIODE_BLOCK_ON");
            ENCHANTMENT_TABLE = Material.valueOf("ENCHANTMENT_TABLE");
            FIREBALL = Material.valueOf("FIREBALL");
        }

    }

    private void setupPlaceholderAPI() {
        Plugin clip = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (clip != null && clip.isEnabled()) {
            this.clipPlaceholderAPIManager = new ClipPlaceholderAPIManager();
            if (this.clipPlaceholderAPIManager.register()) {
                PlaceholderApi = true;
                log(Level.INFO, "Successfully registered placeholders with PlaceholderAPI.");
            } else {
                PlaceholderApi = false;
            }
        } else {
            PlaceholderApi = false;
        }

        Plugin mvdw = getServer().getPluginManager().getPlugin("MVdWPlaceholderAPI");
        if (mvdw != null && mvdw.isEnabled()) {
            this.mvdwPlaceholderAPIManager = true;
            log(Level.INFO, "Found MVdWPlaceholderAPI. Adding hooks.");
        }
    }

    public void changeItemIDSInConfig() {
        log("Starting conversion of legacy material in config to 1.13 materials.");

        replaceStringInConfig("fperm-gui.relation.materials.recruit", "WOOD_SWORD", "WOODEN_SWORD");
        replaceStringInConfig("fperm-gui.relation.materials.normal", "GOLD_SWORD", "GOLDEN_SWORD");
        replaceStringInConfig("fperm-gui.relation.materials.ally", "GOLD_AXE", "GOLDEN_AXE");
        replaceStringInConfig("fperm-gui.relation.materials.neutral", "WOOD_AXE", "WOODEN_AXE");

        ConfigurationSection actionMaterialsConfigSection = getConfig().getConfigurationSection("fperm-gui.action.materials");
        Set<String> actionMaterialKeys = actionMaterialsConfigSection.getKeys(true);


        for (String key : actionMaterialKeys) {
            replaceStringInConfig("fperm-gui.action.materials." + key, "STAINED_GLASS", "GRAY_STAINED_GLASS");
        }

        replaceStringInConfig("fperm-gui.dummy-items.0.material", "STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE");
        replaceStringInConfig("fwarp-gui.dummy-items.0.material", "STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE");

        replaceStringInConfig("fupgrades.MainMenu.DummyItem.Type", "STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE");
        replaceStringInConfig("fupgrades.MainMenu.EXP.EXPItem.Type", "EXP_BOTTLE", "EXPERIENCE_BOTTLE");
        replaceStringInConfig("fupgrades.MainMenu.Spawners.SpawnerItem.Type", "MOB_SPAWNER", "SPAWNER");

        replaceStringInConfig("fperm-gui.action.access.allow", "LIME", "LIME_STAINED_GLASS");
        replaceStringInConfig("fperm-gui.action.access.deny", "RED", "RED_STAINED_GLASS");
        replaceStringInConfig("fperm-gui.action.access.undefined", "CYAN", "CYAN_STAINED_GLASS");
    }

    public void replaceStringInConfig(String path, String stringToReplace, String replacementString) {
        if (getConfig().getString(path).equals(stringToReplace)) {
            // SavageFactionsPlugin.plugin.log("Replacing legacy material '" + stringToReplace + "' with '" + replacementString + "' for config node '" + path + "'.");
            // log("Replacing legacy material '" + stringToReplace + "' with '" + replacementString + "' for config node '" + path + "'.");

            getConfig().set(path, replacementString);
        }
    }

    public boolean isClipPlaceholderAPIHooked() {
        return this.clipPlaceholderAPIManager != null;
    }

    public boolean isMVdWPlaceholderAPIHooked() {
        return this.mvdwPlaceholderAPIManager;
    }

    private boolean setupPermissions() {
        try {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            if (rsp != null) {
                perms = rsp.getProvider();
            }
        } catch (NoClassDefFoundError ex) {
            return false;
        }
        return perms != null;
    }

    @Override
    public GsonBuilder getGsonBuilder() {
        Type mapFLocToStringSetType = new TypeToken<Map<FLocation, Set<String>>>() {
        }.getType();

        Type accessTypeAdatper = new TypeToken<Map<Permissable, Map<PermissableAction, Access>>>() {
        }.getType();

        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().enableComplexMapKeySerialization().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE)
                .registerTypeAdapter(accessTypeAdatper, new PermissionsMapTypeAdapter())
                .registerTypeAdapter(LazyLocation.class, new MyLocationTypeAdapter())
                .registerTypeAdapter(mapFLocToStringSetType, new MapFLocToStringSetTypeAdapter())
                .registerTypeAdapter(Inventory.class, new InventoryTypeAdapter())
                .registerTypeAdapter(Location.class, new LocationTypeAdapter())
                .registerTypeAdapterFactory(EnumTypeAdapter.ENUM_FACTORY);
    }

    @Override
    public void onDisable() {
        if (AutoLeaveTask != null) {
            getServer().getScheduler().cancelTask(AutoLeaveTask);
            AutoLeaveTask = null;
        }

        FactionsBlockListener.warBanners.values().forEach(IWarBanner::remove);

        SqlBuilder.getInstance().close();
        super.onDisable();
    }

    public void startAutoLeaveTask(boolean restartIfRunning) {
        if (AutoLeaveTask != null) {
            if (!restartIfRunning) {
                return;
            }
            this.getServer().getScheduler().cancelTask(AutoLeaveTask);
        }

        if (Conf.autoLeaveRoutineRunsEveryXMinutes > 0.0) {
            long ticks = (long) (20 * 60 * Conf.autoLeaveRoutineRunsEveryXMinutes);
            AutoLeaveTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new AutoLeaveTask(), ticks, ticks);
        }
    }

    @Override
    public void postAutoSave() {
        //Board.getInstance().forceSave(); Not sure why this was there as it's called after the board is already saved.
        Conf.save();
    }

    public ItemStack createItem(Material material, int amount, short datavalue, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, amount, datavalue);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));
        meta.setLore(colorList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createLazyItem(Material material, int amount, short datavalue, String name, String lore) {
        ItemStack item = new ItemStack(material, amount, datavalue);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(SavageFactionsPlugin.plugin.getConfig().getString(name)));
        meta.setLore(colorList(SavageFactionsPlugin.plugin.getConfig().getStringList(lore)));
        item.setItemMeta(meta);
        return item;
    }

    public Economy getEcon() {
        RegisteredServiceProvider<Economy> rsp = SavageFactionsPlugin.plugin.getServer().getServicesManager().getRegistration(Economy.class);
        Economy econ = rsp.getProvider();
        return econ;
    }

    private boolean setupEssentials() {
        SavageFactionsPlugin.plugin.ess = (Essentials) this.getServer().getPluginManager().getPlugin("Essentials");
        return SavageFactionsPlugin.plugin.ess == null;
    }

    @Override
    public boolean logPlayerCommands() {
        return Conf.logPlayerCommands;
    }

    @Override
    public boolean handleCommand(CommandSender sender, String commandString, boolean testOnly) {
        return sender instanceof Player && FactionsPlayerListener.preventCommand(commandString, (Player) sender) || super.handleCommand(sender, commandString, testOnly);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (split.length == 0) {
            return handleCommand(sender, "/f help", false);
        }

        // otherwise, needs to be handled; presumably another plugin directly ran the command
        String cmd = Conf.baseCommandAliases.isEmpty() ? "/f" : "/" + Conf.baseCommandAliases.get(0);
        return handleCommand(sender, cmd + " " + TextUtil.implode(Arrays.asList(split), " "), false);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer((Player) sender);
        List<String> completions = new ArrayList<>();
        String cmd = Conf.baseCommandAliases.isEmpty() ? "/f" : "/" + Conf.baseCommandAliases.get(0);
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.remove(argsList.size() - 1);
        String cmdValid = (cmd + " " + TextUtil.implode(argsList, " ")).trim();
        MCommand<?> commandEx = cmdBase;
        List<MCommand<?>> commandsList = cmdBase.subCommands;

        if (Board.getInstance().getFactionAt(new FLocation(fPlayer.getPlayer().getLocation())) == Factions.getInstance().getWarZone()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou cannot use autocomplete in warzone."));
            return new ArrayList<>();
        }

        for (; !commandsList.isEmpty() && !argsList.isEmpty(); argsList.remove(0)) {
            String cmdName = argsList.get(0).toLowerCase();
            MCommand<?> commandFounded = commandsList.stream()
                    .filter(c -> c.aliases.contains(cmdName))
                    .findFirst().orElse(null);

            if (commandFounded != null) {
                commandEx = commandFounded;
                commandsList = commandFounded.subCommands;
            } else break;
        }

        if (argsList.isEmpty()) {
            for (MCommand<?> subCommand : commandEx.subCommands) {
                subCommand.setCommandSender(sender);
                if (handleCommand(sender, cmdValid + " " + subCommand.aliases.get(0), true)
                        && subCommand.visibility != CommandVisibility.INVISIBLE
                        && subCommand.validSenderType(sender, false)
                        && subCommand.validSenderPermissions(sender, false))
                    completions.addAll(subCommand.aliases);
            }
        }

        String lastArg = args[args.length - 1].toLowerCase();

        completions = completions.stream()
                .filter(m -> m.toLowerCase().startsWith(lastArg))
                .collect(Collectors.toList());

        return completions;
    }

    // -------------------------------------------- //
    // Functions for other plugins to hook into
    // -------------------------------------------- //

    // This value will be updated whenever new hooks are added
    public int hookSupportVersion() {
        return 3;
    }

    // If another plugin is handling insertion of chat tags, this should be used to notify Factions
    public void handleFactionTagExternally(boolean notByFactions) {
        Conf.chatTagHandledByAnotherPlugin = notByFactions;
    }

    // Simply put, should this chat event be left for Factions to handle? For now, that means players with Faction Chat
    // enabled or use of the Factions f command without a slash; combination of isPlayerFactionChatting() and isFactionsCommand()

    public boolean shouldLetFactionsHandleThisChat(AsyncPlayerChatEvent event) {
        return event != null && (isPlayerFactionChatting(event.getPlayer()) || isFactionsCommand(event.getMessage()));
    }


    // Does player have Faction Chat enabled? If so, chat plugins should preferably not do channels,
    // local chat, or anything else which targets individual recipients, so Faction Chat can be done
    public boolean isPlayerFactionChatting(Player player) {
        if (player == null) {
            return false;
        }
        FPlayer me = FPlayers.getInstance().getByPlayer(player);

        return me != null && me.getChatMode().isAtLeast(ChatMode.ALLIANCE);
    }

    // Is this chat message actually a Factions command, and thus should be left alone by other plugins?

    // TODO: GET THIS BACK AND WORKING

    public boolean isFactionsCommand(String check) {
        return !(check == null || check.isEmpty()) && this.handleCommand(null, check, true);
    }

    // Get a player's faction tag (faction name), mainly for usage by chat plugins for local/channel chat
    public String getPlayerFactionTag(Player player) {
        return getPlayerFactionTagRelation(player, null);
    }

    // Same as above, but with relation (enemy/neutral/ally) coloring potentially added to the tag
    public String getPlayerFactionTagRelation(Player speaker, Player listener) {
        String tag = "~";

        if (speaker == null) {
            return tag;
        }

        FPlayer me = FPlayers.getInstance().getByPlayer(speaker);
        if (me == null) {
            return tag;
        }

        // if listener isn't set, or config option is disabled, give back uncolored tag
        if (listener == null || !Conf.chatTagRelationColored) {
            tag = me.getChatTag().trim();
        } else {
            FPlayer you = FPlayers.getInstance().getByPlayer(listener);
            if (you == null) {
                tag = me.getChatTag().trim();
            } else  // everything checks out, give the colored tag
            {
                tag = me.getChatTag(you).trim();
            }
        }
        if (tag.isEmpty()) {
            tag = "~";
        }

        return tag;
    }

    public String color(String line) {
        return ChatColor.translateAlternateColorCodes('&', line);
    }

    //colors a string list
    public List<String> colorList(List<String> lore) {
        for (int i = 0; i <= lore.size() - 1; i++) {
            lore.set(i, color(lore.get(i)));
        }
        return lore;
    }

    public String getPrimaryGroup(OfflinePlayer player) {
        return perms == null || !perms.hasGroupSupport() ? " " : perms.getPrimaryGroup(Bukkit.getWorlds().get(0).toString(), player);
    }

    public void debug(Level level, String s) {
        if (getConfig().getBoolean("debug", false)) {
            getLogger().log(level, s);
        }
    }

    public void debug(String s) {
        debug(Level.INFO, s);
    }
}
