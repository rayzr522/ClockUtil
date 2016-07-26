
package com.rayzr522.clockutil;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CommandMenu implements ICommand {

	private ClockUtil plugin;

	public CommandMenu(ClockUtil plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can use this command");
			return true;
		}

		Player player = (Player) sender;

		FileConfiguration config = plugin.getConfig();

		if (args.length < 1) {

			if (config.contains("settings.defaultMenu")) {

				String defaultMenu = config.getString("settings.defaultMenu");

				if (config.contains("settings." + defaultMenu + ".title")) {

					plugin.openInventory(player, defaultMenu);

				} else {

					showHelp(player);

				}

			} else {

				showHelp(player);

			}

		} else if (args.length > 0) {

			String menuName = args[0].toLowerCase();

			if (config.contains("settings." + menuName + ".title")) {

				plugin.openInventory(player, menuName);

			} else {

				player.sendMessage(ChatColor.RED + "No such menu!");

			}

		}

		return true;

	}

	@Override
	public void showHelp(CommandSender sender) {

		sender.sendMessage(ChatColor.GOLD + "USAGE:" + ChatColor.YELLOW + "/menu [name]");

	}

}
