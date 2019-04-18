package com.massivecraft.factions.zcore.persist;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.zcore.MPlugin;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

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

        try {
            Factions.getInstance().save();
            FPlayers.getInstance().save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Board.getInstance().forceSave(false);
        plugin.postAutoSave();
        running = false;
    }
}
