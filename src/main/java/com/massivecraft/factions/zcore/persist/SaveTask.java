package com.massivecraft.factions.zcore.persist;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.zcore.MPlugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveTask implements Runnable {
    private static boolean running = false;
    private final MPlugin plugin;

    public void run() {
        if (!plugin.getAutoSave() || running) {
            return;
        }
        running = true;
        plugin.preAutoSave();
        Factions.getInstance().forceSave(false);
        FPlayers.getInstance().forceSave(false);
        Board.getInstance().forceSave(false);
        plugin.postAutoSave();
        running = false;
    }
}
