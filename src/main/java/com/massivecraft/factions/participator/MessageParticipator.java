package com.massivecraft.factions.participator;

import com.massivecraft.factions.zcore.util.TL;

public interface MessageParticipator {

    void msg(String str, Object... args);

    void msg(TL translation, Object... args);
}
