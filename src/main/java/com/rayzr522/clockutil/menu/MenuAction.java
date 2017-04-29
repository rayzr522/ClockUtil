package com.rayzr522.clockutil.menu;

import com.rayzr522.clockutil.menu.action.MenuActionCommand;
import com.rayzr522.clockutil.menu.action.MenuActionMenu;
import com.rayzr522.clockutil.utils.TextUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MenuAction {

    private static final String SEPARATOR = "/_azX\\?";

    private ActionType type;
    private String data;

    private MenuAction(ActionType type, String data) {

        this.type = type;
        this.data = data.trim();

    }

    public static List<MenuAction> getActions(List<String> stringList) {

        List<MenuAction> actions = new ArrayList<MenuAction>();

        for (String action : stringList) {

            String[] split = action.replaceFirst(":", SEPARATOR).split(SEPARATOR);

            ActionType type = ActionType.valueOf(TextUtils.enumFormat(split[0]));
            actions.add(new MenuAction(type, split.length > 1 ? split[1].trim() : ""));

        }

        return actions;

    }

    public void execute(Player player) {

        switch (type) {

            case COMMAND:
                new MenuActionCommand().execute(player, data);
                break;
            case MENU:
                new MenuActionMenu().execute(player, data);
                break;
            default:

        }

    }

    public ActionType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public enum ActionType {

        COMMAND,
        MENU

    }

}
