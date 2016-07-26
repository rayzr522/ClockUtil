
package com.rayzr522.clockutil;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public interface ICommand extends CommandExecutor {

	public void showHelp(CommandSender sender);

}
