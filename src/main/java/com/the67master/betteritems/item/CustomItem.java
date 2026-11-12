package com.the67master.betteritems.item;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class CustomItem {
    private final String id;
    private final Material material;
    private final int amount;
    private final String permission;
    private final int cooldown;
    private final int cooldownBarTicks;
    private final ItemStack stack;
    private final ConfigurationSection section;

    public CustomItem(String id, Material material, int amount, String permission, int cooldown,
                      int cooldownBarTicks, ItemStack stack, ConfigurationSection section) {
        this.id=id; this.material=material; this.amount=amount; this.permission=permission;
        this.cooldown=cooldown; this.cooldownBarTicks=cooldownBarTicks; this.stack=stack; this.section=section;
    }
    public String getId(){return id;}
    public Material getMaterial(){return material;}
    public int getAmount(){return amount;}
    public String getPermission(){return permission;}
    public int getCooldown(){return cooldown;}
    public int getCooldownBarTicks(){return cooldownBarTicks;}
    public ConfigurationSection getSection(){return section;}
    public List<String> getLore(){return section.getStringList("lore");}
    public ItemStack getItemStack(){return stack.clone();}
}
