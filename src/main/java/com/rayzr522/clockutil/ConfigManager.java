package com.rayzr522.clockutil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ConfigManager {
    private ClockUtil plugin;

    public ConfigManager(ClockUtil plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();
    }

    public boolean configYmlExists() {
        return getFile("config.yml").exists();
    }

    public File getFile(String path) {
        return new File(plugin.getDataFolder(), path.replace('/', File.separatorChar));
    }

    public void backupConfig() throws IOException {
        if (!configYmlExists()) {
            return;
        }

        File backupFile = getFile(String.format("config-backup.%s.yml", getDateString()));

        // Shouldn't happen, as the files are named by exact time. (down to the second)
        if (backupFile.exists()) {
            throw new IllegalStateException("A backup file already existed @ " + backupFile.getAbsolutePath());
        }

        File config = getFile("config.yml");

        Files.move(config.toPath(), backupFile.toPath());

        plugin.saveResource("config.yml", true);

    }

    private String getDateString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-y-H-m-s", Locale.ENGLISH);
        return ZonedDateTime.now().format(formatter);
    }

}
