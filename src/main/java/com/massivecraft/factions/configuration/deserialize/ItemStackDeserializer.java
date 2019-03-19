package com.massivecraft.factions.configuration.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemStackDeserializer extends StdDeserializer<ItemStack> {

    public ItemStackDeserializer() {
        super(ItemStack.class);
    }

    @Override
    public ItemStack deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        Material material = Material.matchMaterial(node.get("type").asText());
        int amount = node.get("amount").asInt();
        short durability = node.get("durability").shortValue();

        ItemStack itemStack = new ItemStack(material, amount, durability);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (node.has("displayName")) {
            itemMeta.setDisplayName(node.get("displayName").asText());
        }

        if (node.has("enchantments")) {
            node.get("enchantments").fields().forEachRemaining((entry) ->
                    itemStack.addEnchantment(Enchantment.getByName(entry.getKey()), entry.getValue().asInt()));
        }

        if (node.has("lore")) {
            List<String> lore = new ArrayList<>();

            for (JsonNode line : node.get("lore")) {
                lore.add(line.asText());
            }

            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
