
package com.rayzr522.clockutil.menu;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.rayzr522.clockutil.exception.ConfigFormatException;
import com.rayzr522.clockutil.utils.ItemUtils;

public class MenuItem {

    private ItemStack        item;
    private int              slot;
    private String           permission;
    private List<MenuAction> actions;

    private MenuItem(ItemStack item, int slot, String permission, List<MenuAction> actions) {

        this.item = item;
        this.slot = slot;
        this.permission = permission;
        this.actions = actions;

    }

    public static MenuItem loadFromConfig(ConfigurationSection section) throws ConfigFormatException {

        if (section == null) {

            throw new IllegalArgumentException("Parameter 'section' must not be null");

        } else if (!section.contains("type")) {

            throw new ConfigFormatException("MenuItem", "type");

        } else if (!section.contains("slot")) {

            throw new ConfigFormatException("MenuItem", "slot");

        } else if (!section.contains("name")) {

            throw new ConfigFormatException("MenuItem", "name");

        } else if (!section.contains("actions")) {

            throw new ConfigFormatException("MenuItem", "actions");

        }

        String[] typeString = section.getString("type").split(":");
        short damage = 0;

        if (typeString.length > 1 && typeString[1] != null) {
            String str = typeString[1].trim().replaceAll("[a-zA-Z]", "");
            if (str.length() > 0) {
                damage = Short.parseShort(typeString[1].replaceAll("[a-zA-Z]", ""));
            }
        }

        ItemStack item = new ItemStack(ItemUtils.getType(typeString[0]), 1, damage);
        ItemUtils.nameItem(item, section.getString("name"));

        int slot = 0;
        String slotText = section.getString("slot").replace(" ", "");
        if (slotText.indexOf(",") != -1) {
            String[] split = slotText.split(",");
            try {
                slot = Integer.parseInt(split[0]) - 1 + (Integer.parseInt(split[1]) - 1) * 9;
            } catch (Exception e) {

            }
        } else {
            slot = section.getInt("slot") - 1;
        }

        return new MenuItem(item, slot, section.getString("permission"), MenuAction.getActions(section.getStringList("actions")));

    }

    public ItemStack getItem() {

        return item;

    }

    public int getSlot() {

        return slot;

    }

    public List<MenuAction> getActions() {

        return actions;

    }

    public boolean hasPermission(Player player) {

        return (permission == null || permission.equals("") || player.hasPermission(permission));

    }

    public void executeActions(Player player) {

        for (MenuAction action : actions) {

            action.execute(player);

        }

    }

}
