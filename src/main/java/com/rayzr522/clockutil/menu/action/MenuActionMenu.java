
package com.rayzr522.clockutil.menu.action;

import org.bukkit.entity.Player;

import com.rayzr522.clockutil.ClockUtil;
import com.rayzr522.clockutil.menu.IMenuAction;

public class MenuActionMenu implements IMenuAction {

    @Override
    public void execute(Player player, String data) {

        ClockUtil.openInventory(player, data);

    }

}
