package com.the67master.betteritems.item;

import com.the67master.betteritems.BetterItems;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.ChatColor;

import java.util.*;

public class ItemManager {
    private final BetterItems plugin;
    private final Map<String, CustomItem> items = new LinkedHashMap<String, CustomItem>();

    public ItemManager(BetterItems plugin) { this.plugin = plugin; }

    public void loadItems() {
        items.clear();
        ConfigurationSection root = plugin.getConfigFromItems().getConfigurationSection("items");
        if (root == null) return;
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            Material material = Material.matchMaterial(section.getString("material", "STONE"));
            if (material == null) {
                plugin.getLogger().warning("Invalid material for item: " + id);
                continue;
            }
            int amount = Math.max(1, Math.min(64, section.getInt("amount", 1)));
            String name = plugin.color(section.getString("name", id));
            List<String> lore = new ArrayList<String>();
            for (String line : section.getStringList("lore")) lore.add(plugin.color(line));
            lore.add(ChatColor.BLACK + "betteritems:" + id);

            ItemStack stack = new ItemStack(material, amount);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.spigot().setUnbreakable(section.getBoolean("unbreakable", false));
            if (section.getBoolean("glow", false)) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            stack.setItemMeta(meta);

            ConfigurationSection ability = section.getConfigurationSection("ability");
            String trigger = ability == null ? "NONE" : ability.getString("trigger", "NONE");
            String type = ability == null ? "NONE" : ability.getString("type", "NONE");
            String particle = ability == null ? "" : ability.getString("particle", "");
            String sound = ability == null ? "" : ability.getString("sound", "");
            items.put(id.toLowerCase(Locale.ROOT), new CustomItem(id, material, amount, name, lore,
                    section.getBoolean("unbreakable", false), section.getBoolean("glow", false),
                    section.getString("permission", ""), section.getInt("cooldown", 0),
                    trigger, type, particle, sound, stack));
        }
    }

    public CustomItem getItem(String id) { return items.get(id.toLowerCase(Locale.ROOT)); }
    public Collection<CustomItem> getItems() { return Collections.unmodifiableCollection(items.values()); }

    public CustomItem identify(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta() || !stack.getItemMeta().hasLore()) return null;
        for (String line : stack.getItemMeta().getLore()) {
            String plain = ChatColor.stripColor(line);
            if (plain != null && plain.startsWith("betteritems:")) return getItem(plain.substring("betteritems:".length()));
        }
        return null;
    }
}
