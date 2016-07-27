
package com.rayzr522.clockutil.menu.action;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.rayzr522.clockutil.menu.IMenuAction;

public class MenuActionCommand implements IMenuAction {

	@Override
	public void execute(Player player, String data) {

		if (data.startsWith("c:")) {
			runCommand(Bukkit.getConsoleSender(), player, data.substring(2));
		} else {
			runCommand(player, player, data);
		}

	}

	private void runCommand(CommandSender sender, Player player, String command) {

		Bukkit.getServer().dispatchCommand(sender, replaceModifiers(command, player));

	}

	private String replaceModifiers(String data, Player player) {

		data = data.replace("<name>", player.getName());
		data = data.replace("<display>", player.getDisplayName());
		data = data.replace("<health>", (int) Math.floor(player.getHealth()) + "");
		data = data.replace("<food>", player.getFoodLevel() + "");

		return data;

	}

}
