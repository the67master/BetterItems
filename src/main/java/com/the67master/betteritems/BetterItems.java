package com.the67master.betteritems;

import com.the67master.betteritems.command.BetterItemsCommand;
import com.the67master.betteritems.item.ItemManager;
import com.the67master.betteritems.listener.AbilityListener;
import com.the67master.betteritems.listener.EditorListener;\nimport com.the67master.betteritems.recipe.RecipeManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public final class BetterItems extends JavaPlugin {
    private ItemManager itemManager;
    private FileConfiguration itemsConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!new File(getDataFolder(), "items.yml").exists()) saveResource("items.yml", false);
        reloadItemsConfig();
        this.itemManager = new ItemManager(this);
        this.itemManager.loadItems();\n        new RecipeManager(this).registerAll();

        BetterItemsCommand command = new BetterItemsCommand(this);
        PluginCommand pluginCommand = getCommand("betteritems");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
        getServer().getPluginManager().registerEvents(new AbilityListener(this), this);
        getServer().getPluginManager().registerEvents(new EditorListener(this), this);
        getLogger().info("BetterItems enabled. Loaded " + itemManager.getItems().size() + " custom items.");
    }

    public void reloadItemsConfig() {
        File file = new File(getDataFolder(), "items.yml");
        if (!file.exists()) saveResource("items.yml", false);
        itemsConfig = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfigFromItems() { return itemsConfig; }
    public ItemManager getItemManager() { return itemManager; }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    public String message(String key) {
        return color(getConfig().getString("settings.prefix", "&8[&bBetterItems&8] ")
                + getConfig().getString("settings." + key, ""));
    }
}
