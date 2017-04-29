package com.rayzr522.clockutil.menu;

import com.rayzr522.clockutil.utils.ItemUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class MenuItem {
    private ItemStack item;
    private int slot;
    private String permission;
    private List<MenuAction> actions;

    private MenuItem(ItemStack item, int slot, String permission, List<MenuAction> actions) {
        this.item = item;
        this.slot = slot;
        this.permission = permission;
        this.actions = actions;
    }

    public static MenuItem loadFromConfig(ConfigurationSection section) {
        Objects.requireNonNull(section, "section cannot be null!");
        Validate.isTrue(section.contains("type"), "MenuItem is missing 'type'!");
        Validate.isTrue(section.contains("slot"), "MenuItem is missing 'slot'!");
        Validate.isTrue(section.contains("name"), "MenuItem is missing 'name'!");
        Validate.isTrue(section.contains("actions"), "MenuItem is missing 'actions'!");

        String[] typeString = section.getString("type").split(":");
        short damage = 0;

        if (typeString.length > 1 && typeString[1] != null) {
            String str = typeString[1].trim().replaceAll("[^0-9]", "");
            if (str.length() > 0) {
                damage = Short.parseShort(typeString[1].replaceAll("[a-zA-Z]", ""));
            }
        }

        ItemStack item = new ItemStack(ItemUtils.getType(typeString[0]), 1, damage);
        ItemUtils.nameItem(item, section.getString("name"));

        int slot = 0;
        String slotText = section.getString("slot").trim();

        if (slotText.indexOf(",") != -1) {
            String[] split = slotText.split(",");
            try {
                slot = Integer.parseInt(split[0].trim()) - 1 + (Integer.parseInt(split[1].trim()) - 1) * 9;
            } catch (Exception e) {

            }
        } else {
            slot = section.getInt("slot") - 1;
        }

        List<MenuAction> actions = MenuAction.getActions(section.getStringList("actions"));

        return new MenuItem(item, slot, section.getString("permission"), actions);
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
        return permission == null || permission.equals("") || player.hasPermission(permission);
    }

    public void executeActions(Player player) {
        for (MenuAction action : actions) {
            action.execute(player);
        }
    }

}
