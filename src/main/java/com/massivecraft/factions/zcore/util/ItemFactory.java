package com.massivecraft.factions.zcore.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class ItemFactory {
    private static ItemFactory instance;

    private ItemFactory(){
    }

    public byte[] compressByteArray(byte[] bytes){

        ByteArrayOutputStream baos = null;
        Deflater dfl = new Deflater();
        dfl.setLevel(Deflater.BEST_COMPRESSION);
        dfl.setInput(bytes);
        dfl.finish();
        baos = new ByteArrayOutputStream();
        byte[] tmp = new byte[4*1024];
        try{
            while(!dfl.finished()){
                int size = dfl.deflate(tmp);
                baos.write(tmp, 0, size);
            }
        } catch (Exception ex){

        } finally {
            try{
                if(baos != null) baos.close();
            } catch(Exception ex){}
        }

        return baos.toByteArray();
    }

    public String serializeItem(ItemStack... items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            // Write the size of the inventory
            dataOutput.write(items.length);

            // Save every element in the list
            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }

            // Serialize that array
            dataOutput.close();
            return outputStream.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }

    }

    public ItemStack deserializeItem(String data) {
        return null;
    }

    public static ItemFactory getInstance() {
        return instance == null ? (instance = new ItemFactory()) : instance;
    }
}
