
package com.rayzr522.clockutil;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Strings;
import com.rayzr522.clockutil.exception.ConfigFormatException;
import com.rayzr522.clockutil.menu.Menu;
import com.rayzr522.clockutil.menu.MenuHolder;
import com.rayzr522.clockutil.menu.MenuItem;
import com.rayzr522.clockutil.utils.ItemUtils;
import com.rayzr522.clockutil.utils.Msg;
import com.rayzr522.clockutil.utils.TextUtils;

public class ClockUtil extends JavaPlugin implements Listener {

    public static final String        HORIZONTAL_BAR          = ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + Strings.repeat("-", 53);
    public static boolean             DEBUG                   = false;
    public static boolean             SILENT_MENU_PERMISSIONS = false;

    public static String              DEFAULT_MENU            = "";

    private static ClockUtil          plugin;

    private HashMap<Material, String> items;
    private HashMap<String, Menu>     menus;

    private ConfigManager             cm;
    private Logger                    logger;

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
            System.err.println("FATAL CONFIG ERROR");
            System.err.println("'menus' is missing!");
            cm.backupConfig();
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

            } catch (ConfigFormatException e) {

                System.err.println("Failed to load menu '" + key + "':");
                e.printStackTrace();

            }

        }

    }

    // Load in all the items that are associated with menus
    private void loadItems() {

        ConfigurationSection itemsSection = getConfig().getConfigurationSection("items");

        if (itemsSection == null) {
            System.err.println("FATAL CONFIG ERROR");
            System.err.println("'items' is missing!");
            cm.backupConfig();
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

        // Make sure that there IS a menu for that name
        if (name != null && menus.containsKey(name)) {
            return menus.get(name);
        }
        return null;

    }

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

        if (!(e.getInventory().getHolder() instanceof MenuHolder)) {
            return;
        }

        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }

        e.setCancelled(true);

        MenuHolder holder = (MenuHolder) e.getInventory().getHolder();
        Menu menu = holder.getMenu();

        MenuItem item = menu.getItem(e.getRawSlot());

        if (item == null) {
            return;
        }

        Player player = (Player) e.getWhoClicked();

        player.closeInventory();

        item.executeActions(player);

    }

    /*
     * The old code: public ItemStack nameItem(ItemStack item, String name) {
     * 
     * ItemMeta im = item.getItemMeta();
     * im.setDisplayName(TextUtils.colorize("&r" + name)); item.setItemMeta(im);
     * 
     * return item;
     * 
     * }
     * 
     * public int getRows(String name) {
     * 
     * if (getConfig().contains("settings." + name + ".rows")) { if
     * (getConfig().getInt("settings." + name + ".rows") <= 6) { return
     * getConfig().getInt("settings." + name + ".rows"); } else { return 6; } }
     * else { return 1; }
     * 
     * }
     * 
     * public String replaceModifiers(Player player, String command) {
     * 
     * return command.replace("<name>", player.getName());
     * 
     * }
     * 
     * public String getTitle(String name) {
     * 
     * if (getConfig().contains("settings." + name + ".title")) { return
     * getConfig().getString("settings." + name + ".title"); } else { return "";
     * }
     * 
     * }
     * 
     * public Material getMaterial(String type) {
     * 
     * try {
     * 
     * return Material.valueOf(type.toUpperCase().trim().replace(" ", "_"));
     * 
     * } catch (Exception e) {
     * 
     * return Material.STONE;
     * 
     * }
     * 
     * }
     * 
     * 
     * 
     * public void openInventory(Player player, String name) {
     * 
     * Menu menu = getMenu(name);
     * 
     * Inventory inventory;
     * 
     * inventory = getServer().createInventory(player, getRows(name) * 9,
     * TextUtils.colorize(getTitle(name)));
     * 
     * ConfigurationSection menuSection =
     * getConfig().getConfigurationSection("settings." + name);
     * 
     * for (int slot = 0; slot < inventory.getSize() + 1; slot++) {
     * 
     * if (menuSection.contains((slot + 1) + "")) {
     * 
     * ConfigurationSection itemSection =
     * menuSection.getConfigurationSection((slot + 1) + "");
     * 
     * ItemStack item; String[] itemString =
     * itemSection.getString("id").split(":");
     * 
     * short damage = 0;
     * 
     * // id = Integer.parseInt(itemString[0]); Material material =
     * getMaterial(itemString[0]);
     * 
     * if (itemString.length > 1 && itemString[1] != null) { String str =
     * itemString[1].trim().replaceAll("[a-zA-Z]", ""); if (str.length() > 0) {
     * damage = Short.parseShort(itemString[1].replaceAll("[a-zA-Z]", "")); } }
     * 
     * if (itemSection.contains("name")) {
     * 
     * item = nameItem(new ItemStack(material, 1, damage),
     * menuSection.getString((slot + 1) + ".name"));
     * 
     * } else {
     * 
     * item = new ItemStack(material, 1, damage);
     * 
     * }
     * 
     * inventory.setItem(slot, item);
     * 
     * }
     * 
     * }
     * 
     * player.openInventory(inventory); this.openMenus.put(player.getUniqueId(),
     * name); }
     * 
     * @EventHandler(priority = EventPriority.HIGH) public void
     * onPlayerInteract(PlayerInteractEvent e) {
     * 
     * Player player = e.getPlayer();
     * 
     * if (e.hasItem()) { ItemStack item = e.getItem();
     * 
     * if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ||
     * e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
     * 
     * if (getConfig().contains("settings.items." + item.getType().toString()))
     * { openInventory(player, getConfig().getString("settings.items." +
     * item.getType().toString()));
     * 
     * if (getConfig().contains("settings.debug")) {
     * 
     * if (getConfig().getBoolean("settings.debug")) {
     * 
     * player.sendMessage(getConfig().getString("settings.items." +
     * item.getType().toString()));
     * 
     * }
     * 
     * }
     * 
     * return; }
     * 
     * }
     * 
     * }
     * 
     * }
     * 
     * @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     * public void onInventoryClose(InventoryCloseEvent e) {
     * 
     * this.openMenus.remove(e.getPlayer().getUniqueId());
     * 
     * }
     * 
     * @EventHandler(priority = EventPriority.HIGH) public void
     * onInventoryClick(InventoryClickEvent e) {
     * 
     * Player player = (Player) e.getView().getPlayer(); UUID id =
     * player.getUniqueId(); Menu name = this.openMenus.get(id);
     * 
     * ConfigurationSection clock =
     * getConfig().getConfigurationSection("settings." + name);
     * 
     * if (this.openMenus.containsKey(id)) {
     * 
     * e.setCancelled(true); int slot = e.getRawSlot() + 1;
     * 
     * if (clock.contains(slot + "")) {
     * 
     * if (clock.contains(slot + ".commands")) {
     * 
     * for (String command : clock.getStringList(slot + ".commands")) {
     * 
     * if (command.startsWith("c:")) {
     * 
     * getServer().dispatchCommand(getServer().getConsoleSender(),
     * replaceModifiers(player, command.substring(2)));
     * 
     * } else {
     * 
     * getServer().dispatchCommand(player, replaceModifiers(player, command));
     * 
     * }
     * 
     * }
     * 
     * }
     * 
     * if (getConfig().contains("settings." + name + "." + slot + ".inventory"))
     * {
     * 
     * player.closeInventory(); openInventory(player,
     * getConfig().getString("settings." + name + "." + slot + ".inventory"));
     * 
     * }
     * 
     * }
     * 
     * }
     * 
     * }
     * 
     * @EventHandler(priority = EventPriority.HIGH) public void
     * onPlayerMove(PlayerMoveEvent e) {
     * 
     * Player player = e.getPlayer(); UUID id = player.getUniqueId();
     * 
     * if (this.openMenus.containsKey(id)) {
     * 
     * player.setVelocity(new Vector(0.0f, 0.0f, 0.0f));
     * 
     * player.setFallDistance(0.0f);
     * 
     * }
     * 
     * }
     * 
     * 
     */

}
