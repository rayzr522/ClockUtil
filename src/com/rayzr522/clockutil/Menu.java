
package com.rayzr522.clockutil;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.rayzr522.clockutil.exception.ConfigFormatException;
import com.rayzr522.clockutil.utils.TextUtils;

public class Menu {

	private MenuItem[]	items;

	private String		name;
	private String		title;
	private int			rows;

	private Menu(String name, String title, int rows) {

		this.name = name;
		this.title = TextUtils.colorize(title);
		this.rows = rows < 1 ? 1 : (rows > 6 ? 6 : rows);

		items = new MenuItem[rows * 9];

	}

	public static Menu loadFromConfig(ConfigurationSection section) throws ConfigFormatException {

		if (section == null) {
			throw new IllegalArgumentException("Parameter 'section' must not be null");
		} else if (!section.contains("title")) {
			throw new ConfigFormatException("Menu", "title");
		} else if (!section.contains("slot")) {
			throw new ConfigFormatException("Menu", "slot");
		} else if (!section.contains("rows")) {
			throw new ConfigFormatException("Menu", "rows");
		} else if (!section.contains("items")) { throw new ConfigFormatException("Menu", "items"); }

		Menu menu = new Menu(section.getName(), section.getString("title"), section.getInt("rows"));

		Set<String> itemKeys = section.getConfigurationSection("items").getKeys(false);

		for (String key : itemKeys) {

			ConfigurationSection itemSection = section.getConfigurationSection("items." + key);
			MenuItem item = MenuItem.loadFromConfig(itemSection);
			menu.setItem(item.getSlot() - 1, item);

		}

		return menu;

	}

	public void setItem(int slot, MenuItem item) {

		if (slot < items.length) {
			items[slot] = item;
		}

	}

	public MenuItem getItem(int slot) {

		return slot > items.length ? null : items[slot];

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

	public Inventory open(Player player) {

		Inventory inv = Bukkit.createInventory(new MenuHolder(this, Bukkit.createInventory(player, rows * 9)), rows * 9, title);

		player.openInventory(inv);

		return inv;

	}

}
