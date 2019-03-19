package com.massivecraft.factions.listeners;

import com.massivecraft.factions.*;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UnknownFormatConversionException;
import java.util.function.Consumer;
import java.util.logging.Level;

public class FactionsChatListener implements Listener {

    // this is for handling slashless command usage and faction/alliance chat, set at lowest priority so Factions gets to them first
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        Faction myFaction = fPlayer.getFaction();
        String msg = event.getMessage();
        ChatMode chatMode = fPlayer.getChatMode();
        Consumer<String> chatCallback = fPlayer.getChatCallback();

        if (chatCallback != null) {
            event.setCancelled(true);
            chatCallback.accept(msg);
            return;
        }

        if (chatMode == ChatMode.PUBLIC) {
            return;
        }

        String formattedMessage = "";

        switch (chatMode) {
            case MOD:
                formattedMessage = String.format(Conf.modChatFormat, ChatColor.stripColor(fPlayer.getNameAndTag()), msg);
                break;
            case TRUCE:
                formattedMessage = String.format(Conf.truceChatFormat, ChatColor.stripColor(fPlayer.getNameAndTag()), msg);
                break;
            case FACTION:
                formattedMessage = String.format(Conf.factionChatFormat, fPlayer.describeTo(myFaction), msg);
                break;
            case ALLIANCE:
                formattedMessage = String.format(Conf.allianceChatFormat, ChatColor.stripColor(fPlayer.getNameAndTag()), msg);
                break;
        }

        //Is it a MOD chatMode
        if (chatMode == ChatMode.MOD) {
            if (!fPlayer.getRole().isAtLeast(Role.MODERATOR)) {
                // Just in case player gets demoted while in faction chatMode.
                fPlayer.msg(TL.COMMAND_CHAT_MOD_ONLY);
                fPlayer.setChatMode(ChatMode.FACTION);
                event.setCancelled(true);
                return;
            }

            // Iterates only through the factions' members so we enhance performance.
            for (FPlayer fplayer : myFaction.getFPlayers()) {

                if (fplayer.getRole().isAtLeast(Role.MODERATOR)) {
                    fplayer.sendMessage(formattedMessage);
                } else if (fplayer.isSpyingChat() && fPlayer != fplayer) {
                    fplayer.sendMessage("[MCspy]: " + formattedMessage);
                }
            }

            Bukkit.getLogger().log(Level.INFO, ChatColor.stripColor("Mod Chat: " + formattedMessage));

            event.setCancelled(true);
        } else if (chatMode == ChatMode.FACTION) {
            myFaction.sendMessage(formattedMessage);

            Bukkit.getLogger().log(Level.INFO, ChatColor.stripColor("FactionChat " + myFaction.getTag() + ": " + formattedMessage));

            //Send to any players who are spying chatMode
            for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
                if (fplayer.isSpyingChat() && fplayer.getFaction() != myFaction && fPlayer != fplayer) {
                    fplayer.sendMessage("[FCspy] " + myFaction.getTag() + ": " + formattedMessage);
                }
            }

            event.setCancelled(true);
        } else if (chatMode == ChatMode.ALLIANCE) {
            //Send message to our own faction
            myFaction.sendMessage(formattedMessage);

            //Send to all our allies
            for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
                if (myFaction.getRelationTo(fplayer) == Relation.ALLY && !fplayer.isIgnoreAllianceChat()) {
                    fplayer.sendMessage(formattedMessage);
                } else if (fplayer.isSpyingChat() && fPlayer != fplayer) {
                    fplayer.sendMessage("[ACspy]: " + formattedMessage);
                }
            }

            Bukkit.getLogger().log(Level.INFO, ChatColor.stripColor("AllianceChat: " + formattedMessage));

            event.setCancelled(true);
        } else if (chatMode == ChatMode.TRUCE) {
            //Send message to our own faction
            myFaction.sendMessage(formattedMessage);

            //Send to all our truces
            for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
                if (myFaction.getRelationTo(fplayer) == Relation.TRUCE) {
                    fplayer.sendMessage(formattedMessage);
                } else if (fplayer.isSpyingChat() && fplayer != fPlayer) {
                    fplayer.sendMessage("[TCspy]: " + formattedMessage);
                }
            }

            Bukkit.getLogger().log(Level.INFO, ChatColor.stripColor("TruceChat: " + formattedMessage));
            event.setCancelled(true);
        }
    }

    // this is for handling insertion of the player's faction tag, set at highest priority to give other plugins a chance to modify chat first
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Are we to insert the Faction tag into the format?
        // If we are not to insert it - we are done.
        if (!Conf.chatTagEnabled || Conf.chatTagHandledByAnotherPlugin) {
            return;
        }

        Player talkingPlayer = event.getPlayer();
        String msg = event.getMessage();
        String eventFormat = event.getFormat();
        FPlayer me = FPlayers.getInstance().getByPlayer(talkingPlayer);
        int InsertIndex;

        if (!Conf.chatTagReplaceString.isEmpty() && eventFormat.contains(Conf.chatTagReplaceString)) {
            // we're using the "replace" method of inserting the faction tags
            if (eventFormat.contains("[FACTION_TITLE]")) {
                eventFormat = eventFormat.replace("[FACTION_TITLE]", me.getTitle());
            }

            InsertIndex = eventFormat.indexOf(Conf.chatTagReplaceString);
            eventFormat = eventFormat.replace(Conf.chatTagReplaceString, "");
            Conf.chatTagPadAfter = false;
            Conf.chatTagPadBefore = false;
        } else if (!Conf.chatTagInsertAfterString.isEmpty() && eventFormat.contains(Conf.chatTagInsertAfterString)) {
            // we're using the "insert after string" method
            InsertIndex = eventFormat.indexOf(Conf.chatTagInsertAfterString) + Conf.chatTagInsertAfterString.length();
        } else if (!Conf.chatTagInsertBeforeString.isEmpty() && eventFormat.contains(Conf.chatTagInsertBeforeString)) {
            // we're using the "insert before string" method
            InsertIndex = eventFormat.indexOf(Conf.chatTagInsertBeforeString);
        } else {
            // we'll fall back to using the index place method
            InsertIndex = Conf.chatTagInsertIndex;
            if (InsertIndex > eventFormat.length()) {
                return;
            }
        }

        String formatStart = eventFormat.substring(0, InsertIndex) + ((Conf.chatTagPadBefore && !me.getChatTag().isEmpty()) ? " " : "");
        String formatEnd = ((Conf.chatTagPadAfter && !me.getChatTag().isEmpty()) ? " " : "") + eventFormat.substring(InsertIndex);

        String nonColoredMsgFormat = formatStart + me.getChatTag().trim() + formatEnd;

        // Relation Colored?
        if (Conf.chatTagRelationColored) {
            for (Player listeningPlayer : event.getRecipients()) {
                FPlayer you = FPlayers.getInstance().getByPlayer(listeningPlayer);
                String yourFormat = formatStart + me.getChatTag(you).trim() + formatEnd;
                try {
                    listeningPlayer.sendMessage(String.format(yourFormat, talkingPlayer.getDisplayName(), msg));
                } catch (UnknownFormatConversionException ex) {
                    Conf.chatTagInsertIndex = 0;
                    SavageFactionsPlugin.plugin.log(Level.SEVERE, "Critical error in chat message formatting!");
                    SavageFactionsPlugin.plugin.log(Level.SEVERE, "NOTE: This has been automatically fixed right now by setting chatTagInsertIndex to 0.");
                    SavageFactionsPlugin.plugin.log(Level.SEVERE, "For a more proper fix, please read this regarding chat configuration: http://massivecraft.com/plugins/factions/config#Chat_configuration");
                    return;
                }
            }

            // Messages are sent to players individually
            // This still leaves a chance for other plugins to pick it up
            event.getRecipients().clear();
        }
        // Message with no relation color.
        event.setFormat(nonColoredMsgFormat);
    }
}