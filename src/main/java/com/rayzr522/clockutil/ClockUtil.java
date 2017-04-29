package com.rayzr522.clockutil;

import com.google.common.base.Strings;
import com.rayzr522.clockutil.menu.Menu;
import com.rayzr522.clockutil.menu.MenuHolder;
import com.rayzr522.clockutil.menu.MenuItem;
import com.rayzr522.clockutil.utils.ItemUtils;
import com.rayzr522.clockutil.utils.Msg;
import com.rayzr522.clockutil.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClockUtil extends JavaPlugin implements Listener {

    public static final String HORIZONTAL_BAR = ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + Strings.repeat("-", 53);
    public static boolean DEBUG = false;
    public static boolean SILENT_MENU_PERMISSIONS = false;

    public static String DEFAULT_MENU = "";

    private static ClockUtil plugin;

    private HashMap<Material, String> items;
    private HashMap<String, Menu> menus;

    private ConfigManager cm;
    private Logger logger;

    public static boolean openInventory(Player player, String menuName) {
        // Get the menu for the given name (getMenu has a built in null check)
        Menu menu = plugin.getMenu(menuName);

        // If there is no such menu, return false (meaning it failed)
        if (menu == null) {
            return false;
        }

        if (!menu.hasPermission(player)) {
            if (!SILENT_MENU_PERMISSIONS) {
                Msg.player(player, ChatColor.RED + "You don't have permission to open the menu '" + menuName + "'");
            }

            return true;
        }

        menu.open(player);

        return true;
    }

    @Override
    public void onEnable() {

        initVariables();
        registerCommands();
        loadConfig();

        logger.info(TextUtils.stripColor(getInfo()) + " disabled");

        getServer().getPluginManager().registerEvents(this, this);

    }

    private void initVariables() {

        plugin = this;

        cm = new ConfigManager(this);
        logger = getLogger();

        items = new HashMap<Material, String>();
        menus = new HashMap<String, Menu>();

    }

    // Register all the ICommands
    private void registerCommands() {

        getCommand("clockutil").setExecutor(new CommandClockUtil(this));
        getCommand("menu").setExecutor(new CommandMenu(this));
        getCommand("itemtype").setExecutor(new CommandItemType(this));

    }

    // Load config settings and then the menus and items
    public void loadConfig() {

        // Save the defaults
        getConfig().options().copyDefaults(false);
        saveConfig();

        // Whether debug is enabled
        DEBUG = getConfig().getBoolean("settings.debug");
        // Whether or not you should get those nasty messages saying you don't
        // have permission to do something
        SILENT_MENU_PERMISSIONS = getConfig().getBoolean("settings.silentMenuPermissions");
        // Get the default menu
        DEFAULT_MENU = getConfig().getString("settings.defaultMenu");

        // Load the menus and items
        loadMenus();
        loadItems();

    }

    // Load all the menus from the config
    private void loadMenus() {
        ConfigurationSection menusSection = getConfig().getConfigurationSection("menus");

        if (menusSection == null) {
            getLogger().severe("FATAL CONFIG ERROR");
            getLogger().severe("'menus' is missing!");

            try {
                cm.backupConfig();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to back up config file!", e);
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            reload();
            loadMenus();

            return;
        }

        menus.clear();

        for (String key : menusSection.getKeys(false)) {
            try {
                Menu menu = Menu.loadFromConfig(menusSection.getConfigurationSection(key));

                if (menu != null) {
                    menus.put(menu.getName(), menu);
                }
            } catch (IllegalArgumentException e) {
                getLogger().log(Level.SEVERE, "Failed to load menu '" + key + "'!", e);
            }
        }

    }

    // Load in all the items that are associated with menus
    private void loadItems() {

        ConfigurationSection itemsSection = getConfig().getConfigurationSection("items");

        if (itemsSection == null) {
            getLogger().severe("FATAL CONFIG ERROR");
            getLogger().severe("'items' is missing!");

            try {
                cm.backupConfig();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to back up config file!", e);
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            reload();
            loadItems();
            return;
        }

        items.clear();

        for (String key : itemsSection.getKeys(false)) {
            try {
                Material mat = ItemUtils.getType(key);
                String menu = itemsSection.getString(key);

                if (getMenu(itemsSection.getString(key)) == null) {
                    System.err.println("Invalid menu '" + menu + "' for item '" + key + "'");
                } else {
                    items.put(mat, menu);
                }
            } catch (Exception e) {
                System.err.println("Invalid item '" + key + "'");
            }

        }

    }

    public void reload() {

        reloadConfig();
        loadConfig();

    }

    @Override
    public void onDisable() {

        logger.info(TextUtils.stripColor(getInfo()) + " disabled");

    }

    // Just format a pretty little text showing the name and version of the
    // plugin
    public String getInfo() {
        PluginDescriptionFile desc = this.getDescription();
        return ChatColor.RED + "" + ChatColor.BOLD + desc.getName() + ChatColor.YELLOW + " v" + desc.getVersion();
    }

    // Get a menu for a certain name (this is the config name, not the title)
    public Menu getMenu(String name) {
        Objects.requireNonNull(name, "name cannot be null!");

        return menus.get(name);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

        // Only when they right click should this code be called
        if (!e.getAction().toString().startsWith("RIGHT_CLICK")) {
            return;
        }

        // We don't care about this if they aren't holding an item
        if (e.getItem() == null || !items.containsKey(e.getItem().getType())) {
            return;
        }

        String menu = items.get(e.getItem().getType());

        openInventory(e.getPlayer(), menu);

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }

        if (e.getInventory() == null || e.getInventory().getHolder() == null) {
            return;
        }
        Inventory inventory = e.getInventory();

        if (!(inventory.getHolder() instanceof MenuHolder)) {
            return;
        }
        MenuHolder holder = (MenuHolder) inventory.getHolder();

        if (e.getRawSlot() > inventory.getSize()) {
            return;
        }
        e.setCancelled(true);

        Menu menu = holder.getMenu();
        MenuItem item = menu.getItem(e.getRawSlot());

        if (item == null) {
            return;
        }

        Player player = (Player) e.getWhoClicked();

        player.closeInventory();
        item.executeActions(player);

    }

}
