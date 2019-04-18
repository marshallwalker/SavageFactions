package com.massivecraft.factions.configuration.deserialize;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.massivecraft.factions.fperms.Permissable;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;

public class PermissableDeserializer extends KeyDeserializer {

    @Override
    public Permissable deserializeKey(String key, DeserializationContext deserializationContext) {
        key = key.toUpperCase();

        Permissable permissable = Role.fromString(key);

        if (permissable == null) {
            permissable = Relation.fromString(key);
        }

        return permissable;
    }
}
