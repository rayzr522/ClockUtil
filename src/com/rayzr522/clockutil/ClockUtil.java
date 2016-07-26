
package com.rayzr522.clockutil;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.google.common.base.Strings;
import com.rayzr522.clockutil.utils.TextUtils;

public class ClockUtil extends JavaPlugin implements Listener {

	public static final String		HORIZONTAL_BAR	= ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + Strings.repeat("-", 53);
	public static boolean			DEBUG			= false;

	private HashMap<UUID, String>	openClocks;

	private Logger					logger;
	public final ClockUtil			plugin			= this;

	@Override
	public void onEnable() {

		logger = getLogger();

		getConfig().options().copyDefaults(false);
		saveConfig();

		DEBUG = getConfig().getBoolean("settings.debug");

		getCommand("clockutil").setExecutor(new CommandClockUtil(this));
		getCommand("menu").setExecutor(new CommandMenu(this));
		getCommand("itemtype").setExecutor(new CommandItemType(this));

		logger.info(getInfo() + ChatColor.GOLD + " disabled");

		getServer().getPluginManager().registerEvents(this, this);

		openClocks = new HashMap<UUID, String>();

	}

	@Override
	public void onDisable() {

		logger.info(getInfo() + ChatColor.GOLD + " disabled");

	}

	public String getInfo() {

		PluginDescriptionFile desc = this.getDescription();
		return ChatColor.RED + "" + ChatColor.BOLD + desc.getName() + ChatColor.YELLOW + " v" + desc.getVersion();

	}

	public ItemStack nameItem(ItemStack item, String name) {

		ItemMeta im = item.getItemMeta();
		im.setDisplayName(TextUtils.colorize("&r" + name));
		item.setItemMeta(im);

		return item;

	}

	public String replaceModifiers(Player player, String command) {

		return command.replace("<name>", player.getName());

	}

	public String getTitle(String name) {

		if (getConfig().contains("settings." + name + ".title")) {
			return getConfig().getString("settings." + name + ".title");
		} else {
			return "";
		}

	}

	public Material getMaterial(String type) {

		try {

			return Material.valueOf(type.toUpperCase().trim().replace(" ", "_"));

		} catch (Exception e) {

			return Material.STONE;

		}

	}

	public int getRows(String name) {

		if (getConfig().contains("settings." + name + ".rows")) {
			if (getConfig().getInt("settings." + name + ".rows") <= 6) {
				return getConfig().getInt("settings." + name + ".rows");
			} else {
				return 6;
			}
		} else {
			return 1;
		}

	}

	public void openInventory(Player player, String name) {

		Inventory inventory;

		inventory = getServer().createInventory(player, getRows(name) * 9, TextUtils.colorize(getTitle(name)));

		ConfigurationSection menuSection = getConfig().getConfigurationSection("settings." + name);

		for (int slot = 0; slot < inventory.getSize() + 1; slot++) {

			if (menuSection.contains((slot + 1) + "")) {

				ConfigurationSection itemSection = menuSection.getConfigurationSection((slot + 1) + "");

				ItemStack item;
				String[] itemString = itemSection.getString("id").split(":");

				short damage = 0;

				// id = Integer.parseInt(itemString[0]);
				Material material = getMaterial(itemString[0]);

				if (itemString.length > 1 && itemString[1] != null) {
					String str = itemString[1].trim().replaceAll("[a-zA-Z]", "");
					if (str.length() > 0) {
						damage = Short.parseShort(itemString[1].replaceAll("[a-zA-Z]", ""));
					}
				}

				if (itemSection.contains("name")) {

					item = nameItem(new ItemStack(material, 1, damage), menuSection.getString((slot + 1) + ".name"));

				} else {

					item = new ItemStack(material, 1, damage);

				}

				inventory.setItem(slot, item);

			}

		}

		player.openInventory(inventory);
		this.openClocks.put(player.getUniqueId(), name);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent e) {

		Player player = e.getPlayer();

		if (e.hasItem()) {
			ItemStack item = e.getItem();

			if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) {

				if (getConfig().contains("settings.items." + item.getType().toString())) {
					openInventory(player, getConfig().getString("settings.items." + item.getType().toString()));

					if (getConfig().contains("settings.debug")) {

						if (getConfig().getBoolean("settings.debug")) {

							player.sendMessage(getConfig().getString("settings.items." + item.getType().toString()));

						}

					}

					return;
				}

			}

		}

	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent e) {

		this.openClocks.remove(e.getPlayer().getUniqueId());

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent e) {

		Player player = (Player) e.getView().getPlayer();
		UUID id = player.getUniqueId();
		String name = this.openClocks.get(id);

		ConfigurationSection clock = getConfig().getConfigurationSection("settings." + name);

		if (this.openClocks.containsKey(id)) {

			e.setCancelled(true);
			int slot = e.getRawSlot() + 1;

			if (clock.contains(slot + "")) {

				if (clock.contains(slot + ".commands")) {

					for (String command : clock.getStringList(slot + ".commands")) {

						if (command.startsWith("c:")) {

							getServer().dispatchCommand(getServer().getConsoleSender(), replaceModifiers(player, command.substring(2)));

						} else {

							getServer().dispatchCommand(player, replaceModifiers(player, command));

						}

					}

				}

				if (getConfig().contains("settings." + name + "." + slot + ".inventory")) {

					player.closeInventory();
					openInventory(player, getConfig().getString("settings." + name + "." + slot + ".inventory"));

				}

			}

		}

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerMove(PlayerMoveEvent e) {

		Player player = e.getPlayer();
		UUID id = player.getUniqueId();

		if (this.openClocks.containsKey(id)) {

			player.setVelocity(new Vector(0.0f, 0.0f, 0.0f));

			player.setFallDistance(0.0f);

		}

	}

}
