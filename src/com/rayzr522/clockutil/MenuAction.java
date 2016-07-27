
package com.rayzr522.clockutil;

import java.util.ArrayList;
import java.util.List;

import com.rayzr522.clockutil.utils.TextUtils;

public class MenuAction {

	private static final String	SEPARATOR	= "/_azX\\?";

	private ActionType			type;
	private String				data;

	private MenuAction(ActionType type, String data) {

		this.type = type;
		this.data = data;

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
