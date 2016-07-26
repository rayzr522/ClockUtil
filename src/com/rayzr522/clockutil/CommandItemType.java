
package com.rayzr522.clockutil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.rayzr522.clockutil.utils.Msg;

public class CommandItemType implements ICommand {

	@SuppressWarnings("unused")
	private ClockUtil plugin;

	public CommandItemType(ClockUtil plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can use this command");
			return true;
		}

		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInMainHand();

		if (item == null || item.getType() == Material.AIR) {

			Msg.player(sender, ChatColor.RED + "You have to be holding an item");
			return true;

		}

		Msg.player(sender, "&3Item type: &a" + item.getType().toString().toLowerCase().replace("_", " "));
		Msg.player(sender, "&3Amount: &a" + item.getAmount());
		Msg.player(sender, "&3Data value: &a" + item.getDurability());

		return true;

	}

	@Override
	public void showHelp(CommandSender sender) {

		sender.sendMessage(ChatColor.GOLD + "USAGE: " + ChatColor.YELLOW + "/itemtype");

	}

}
