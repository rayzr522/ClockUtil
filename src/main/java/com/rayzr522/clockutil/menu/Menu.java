package com.rayzr522.clockutil.menu;

import com.rayzr522.clockutil.utils.TextUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class Menu {
    private MenuItem[] items;

    private String name;
    private String title;
    private int rows;
    private String permission;

    private Menu(String name, String title, int rows, String permission) {

        this.name = name;
        this.title = TextUtils.colorize(title);
        this.rows = rows < 1 ? 1 : (rows > 6 ? 6 : rows);

        this.permission = permission != null && permission.trim().length() > 0 ? permission.trim() : "";

        items = new MenuItem[rows * 9];

    }

    public static Menu loadFromConfig(ConfigurationSection section) {
        Objects.requireNonNull(section, "section cannot be null!");
        Validate.isTrue(section.contains("title"), "Menu is missing 'title'!");
        Validate.isTrue(section.contains("rows"), "Menu is missing 'rows'!");
        Validate.isTrue(section.contains("items"), "Menu is missing 'items'!");

        Menu menu = new Menu(section.getName(), section.getString("title"), section.getInt("rows"), section.getString("permission"));

        ConfigurationSection itemsSection = section.getConfigurationSection("items");

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);

            MenuItem item = MenuItem.loadFromConfig(itemSection);
            menu.setItem(item.getSlot(), item);
        }

        return menu;

    }

    public void setItem(int slot, MenuItem item) {
        checkSlot(slot);

        items[slot] = item;
    }

    public MenuItem getItem(int slot) {
        checkSlot(slot);

        return slot < 0 || slot >= items.length ? null : items[slot];
    }

    private void checkSlot(int slot) {
        if (slot < 0 || slot >= items.length) {
            throw new IllegalStateException("Slot must be between 0 and " + (items.length - 1));
        }
    }

    public MenuItem[] getItems() {
        return items;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission(Player player) {
        return permission.equals("") || player.hasPermission(permission);
    }

    public Inventory open(Player player) {
        Inventory inv = Bukkit.createInventory(new MenuHolder(this, Bukkit.createInventory(player, rows * 9)), rows * 9, title);

        for (int i = 0; i < items.length; i++) {

            MenuItem menuItem = items[i];
            if (menuItem == null) {
                continue;
            }

            if (!menuItem.hasPermission(player)) {
                continue;
            }

            ItemStack item = menuItem.getItem();
            inv.setItem(i, item);

        }

        player.openInventory(inv);

        return inv;

    }

}
