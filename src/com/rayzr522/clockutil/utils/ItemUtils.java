
package com.rayzr522.clockutil.utils;

import org.bukkit.Material;

public class ItemUtils {

	public static Material getType(String matString) {

		try {

			return Material.valueOf(TextUtils.enumFormat(matString));

		} catch (Exception e) {

			return Material.STONE;

		}

	}

}
