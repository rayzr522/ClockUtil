
package com.rayzr522.clockutil;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public interface ICommand extends CommandExecutor {

    void showHelp(CommandSender sender);

}
