package com.massivecraft.factions.configuration.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.Map;

public class ItemStackSerializer extends StdSerializer<ItemStack> {

    public ItemStackSerializer() {
        super(ItemStack.class);
    }

    @Override
    public void serialize(ItemStack itemStack, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("type", itemStack.getType().name());
        jsonGenerator.writeNumberField("amount", itemStack.getAmount());
        jsonGenerator.writeNumberField("durability", itemStack.getDurability());

        if(itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            if(itemMeta.hasDisplayName()) {
                jsonGenerator.writeStringField("displayName", itemMeta.getDisplayName());
            }

            if(itemMeta.hasEnchants()) {
                jsonGenerator.writeObjectFieldStart("enchantments");

                for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
                    jsonGenerator.writeNumberField(entry.getKey().getName(), entry.getValue());
                }

                jsonGenerator.writeEndObject();
            }

            if(itemMeta.hasLore()) {
                jsonGenerator.writeArrayFieldStart("lore");

                for (String line : itemMeta.getLore()) {
                    jsonGenerator.writeString(line);
                }

                jsonGenerator.writeEndArray();
            }
        }

        jsonGenerator.writeEndObject();
    }
}
