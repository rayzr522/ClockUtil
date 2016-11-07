
package com.rayzr522.clockutil;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.rayzr522.clockutil.utils.Msg;

public class CommandMenu implements ICommand {

    @SuppressWarnings("unused")
    private ClockUtil plugin;

    public CommandMenu(ClockUtil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

        if (!(sender instanceof Player)) {
            Msg.player(sender, ChatColor.RED + "Only players can use this command");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {

            if (!ClockUtil.openInventory(player, ClockUtil.DEFAULT_MENU)) {

                showHelp(player);

            }

        } else if (args.length > 0) {

            String menuName = args[0];

            if (!ClockUtil.openInventory(player, menuName)) {

                Msg.player(player, ChatColor.RED + "No such menu!");

            }

        }

        return true;

    }

    @Override
    public void showHelp(CommandSender sender) {

        sender.sendMessage(ChatColor.GOLD + "USAGE: " + ChatColor.YELLOW + "/menu [name]");

    }

}
