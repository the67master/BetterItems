package com.the67master.betteritems;

import com.the67master.betteritems.command.BetterItemsCommand;
import com.the67master.betteritems.item.ItemManager;
import com.the67master.betteritems.listener.AbilityListener;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterItems extends JavaPlugin {
    private ItemManager itemManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("items.yml", false);
        this.itemManager = new ItemManager(this);
        this.itemManager.loadItems();

        BetterItemsCommand command = new BetterItemsCommand(this);
        PluginCommand pluginCommand = getCommand("betteritems");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
        getServer().getPluginManager().registerEvents(new AbilityListener(this), this);
        getLogger().info("BetterItems enabled. Loaded " + itemManager.getItems().size() + " custom items.");
    }

    @Override
    public void onDisable() {
        getLogger().info("BetterItems disabled.");
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    public String message(String key) {
        return color(getConfig().getString("settings.prefix", "&8[&bBetterItems&8] ")
                + getConfig().getString("settings." + key, ""));
    }
}
