
package com.rayzr522.clockutil.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {

    public static Material getType(String matString) {

        try {

            return Material.valueOf(TextUtils.enumFormat(matString));

        } catch (Exception e) {

            return Material.STONE;

        }

    }

    public static ItemStack nameItem(ItemStack item, String name) {

        ItemMeta im = item.getItemMeta();
        im.setDisplayName(TextUtils.colorize("&r" + name));
        item.setItemMeta(im);

        return item;

    }

}
