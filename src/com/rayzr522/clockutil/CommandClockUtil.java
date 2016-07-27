
package com.rayzr522.clockutil;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandClockUtil implements ICommand {

	private ClockUtil plugin;

	public CommandClockUtil(ClockUtil plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (args.length < 1) {

			showHelp(sender);

		} else if (args.length > 0) {

			String arg = args[0].toLowerCase();

			if (arg.equals("reload")) {

				plugin.reload();
				sender.sendMessage(ChatColor.GOLD + "Config reloaded!");

			} else if (arg.equals("version")) {

				sender.sendMessage(ChatColor.GOLD + "You are using " + plugin.getInfo());

			} else {

				showHelp(sender);

			}

		}

		return true;

	}

	@Override
	public void showHelp(CommandSender sender) {

		sender.sendMessage(ChatColor.GOLD + "USAGE: " + ChatColor.YELLOW + "/clockutil <reload|version>");

	}

}
