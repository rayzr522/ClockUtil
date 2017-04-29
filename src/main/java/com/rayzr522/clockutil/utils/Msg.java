package com.rayzr522.clockutil.utils;

import org.bukkit.command.CommandSender;

public class Msg {

    public static void player(CommandSender player, String msg) {
        player.sendMessage(TextUtils.colorize(msg));
    }

}
