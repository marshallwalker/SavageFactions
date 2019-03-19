package com.massivecraft.factions.zcore.persist;

import com.massivecraft.factions.FWarp;
import com.massivecraft.factions.util.LazyLocation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemoryFWarp implements FWarp {
    protected String name;
    protected String password;
    protected LazyLocation location;

    @Override
    public boolean hasPassword() {
        return password != null && !password.isEmpty();
    }

    @Override
    public boolean isPassword(String password) {
        return hasPassword() ? this.password.equals(password) : password.isEmpty();
    }
}
