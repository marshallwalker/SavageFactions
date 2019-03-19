package com.massivecraft.factions.zcore.nbtapi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonWrapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T deserializeJson(String json, Class<T> type) {
        try {
            if (json == null) {
                return null;
            }

            T obj = objectMapper.readValue(json, type);
            return type.cast(obj);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
