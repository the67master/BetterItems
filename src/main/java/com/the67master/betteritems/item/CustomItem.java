package com.the67master.betteritems.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class CustomItem {
    private final String id;
    private final Material material;
    private final int amount;
    private final String name;
    private final List<String> lore;
    private final boolean unbreakable;
    private final boolean glow;
    private final String permission;
    private final int cooldown;
    private final String trigger;
    private final String abilityType;
    private final String particle;
    private final String sound;
    private final ItemStack stack;

    public CustomItem(String id, Material material, int amount, String name, List<String> lore,
                      boolean unbreakable, boolean glow, String permission, int cooldown,
                      String trigger, String abilityType, String particle, String sound, ItemStack stack) {
        this.id = id; this.material = material; this.amount = amount; this.name = name; this.lore = lore;
        this.unbreakable = unbreakable; this.glow = glow; this.permission = permission; this.cooldown = cooldown;
        this.trigger = trigger; this.abilityType = abilityType; this.particle = particle; this.sound = sound;
        this.stack = stack;
    }
    public String getId() { return id; }
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public String getPermission() { return permission; }
    public int getCooldown() { return cooldown; }
    public String getTrigger() { return trigger; }
    public String getAbilityType() { return abilityType; }
    public String getParticle() { return particle; }
    public String getSound() { return sound; }
    public ItemStack getItemStack() { return stack.clone(); }
}
