package com.massivecraft.factions.zcore.persist.json;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.zcore.persist.MemoryFPlayer;

import java.util.UUID;

public class JSONFPlayer extends MemoryFPlayer {

    public JSONFPlayer(MemoryFPlayer arg0) {
        super(arg0);
    }

    public JSONFPlayer(UUID id) {
        super(id);
    }

    @Override
    public void remove() {
        FPlayers.getInstance().removeById(getId());
    }
}
