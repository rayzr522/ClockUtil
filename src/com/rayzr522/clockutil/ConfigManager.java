
package com.rayzr522.clockutil;

import java.io.File;

public class ConfigManager {

    private ClockUtil plugin;

    public ConfigManager(ClockUtil plugin) {

        this.plugin = plugin;

        if (!configYmlExists()) {

            plugin.saveResource("config.yml", true);

        }

    }

    public boolean configYmlExists() {

        return getFile("config.yml").exists();

    }

    public File getFile(String path) {

        return new File(plugin.getDataFolder() + File.separator + path);

    }

    public void backupConfig() {

        if (!configYmlExists()) {
            return;
        }

        File backupFile = getFile("config-backup.yml");

        if (backupFile.exists()) {
            backupFile.delete();
        }

        File configYml = getFile("config.yml");
        configYml.renameTo(backupFile);

        plugin.saveResource("config.yml", true);

    }

}
