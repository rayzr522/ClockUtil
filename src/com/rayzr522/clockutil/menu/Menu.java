
package com.rayzr522.clockutil.menu;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.rayzr522.clockutil.exception.ConfigFormatException;
import com.rayzr522.clockutil.utils.TextUtils;

public class Menu {

	private MenuItem[]	items;

	private String		name;
	private String		title;
	private int			rows;
	private String		permission;

	private Menu(String name, String title, int rows, String permission) {

		this.name = name;
		this.title = TextUtils.colorize(title);
		this.rows = rows < 1 ? 1 : (rows > 6 ? 6 : rows);

		this.permission = permission != null && permission.trim().length() > 0 ? permission.trim() : "";

		items = new MenuItem[rows * 9];

	}

	public static Menu loadFromConfig(ConfigurationSection section) throws ConfigFormatException {

		if (section == null) {
			throw new IllegalArgumentException("Parameter 'section' must not be null");
		} else if (!section.contains("title")) {
			throw new ConfigFormatException("Menu", "title");
		} else if (!section.contains("rows")) {
			throw new ConfigFormatException("Menu", "rows");
		} else if (!section.contains("items")) { throw new ConfigFormatException("Menu", "items"); }

		Menu menu = new Menu(section.getName(), section.getString("title"), section.getInt("rows"), section.getString("permission"));

		Set<String> itemKeys = section.getConfigurationSection("items").getKeys(false);

		for (String key : itemKeys) {

			ConfigurationSection itemSection = section.getConfigurationSection("items." + key);
			MenuItem item = MenuItem.loadFromConfig(itemSection);
			menu.setItem(item.getSlot(), item);

		}

		return menu;

	}

	public void setItem(int slot, MenuItem item) {

		if (slot < 0 || slot >= items.length) { return; }

		items[slot] = item;

	}

	public MenuItem getItem(int slot) {

		return slot < 0 || slot >= items.length ? null : items[slot];

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
