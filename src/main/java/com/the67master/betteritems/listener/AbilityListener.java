package com.the67master.betteritems.listener;

import com.the67master.betteritems.BetterItems;
import com.the67master.betteritems.item.CustomItem;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityListener implements Listener {
    private final BetterItems plugin;
    private final Map<String, Long> cooldowns = new HashMap<String, Long>();

    public AbilityListener(BetterItems plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        activate(event.getPlayer(), event.getItem(), "RIGHT_CLICK");
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        ItemStack hand = player.getItemInHand();
        activate(player, hand, "HIT");
    }

    private void activate(Player player, ItemStack stack, String trigger) {
        CustomItem item = plugin.getItemManager().identify(stack);
        if (item == null || !item.getTrigger().equalsIgnoreCase(trigger)) return;
        if (!player.hasPermission("betteritems.use") ||
                (!item.getPermission().isEmpty() && !player.hasPermission(item.getPermission()))) {
            player.sendMessage(plugin.message("no-permission"));
            return;
        }
        String key = player.getUniqueId().toString() + ":" + item.getId().toLowerCase();
        long now = System.currentTimeMillis();
        long ready = cooldowns.containsKey(key) ? cooldowns.get(key) : 0L;
        if (ready > now) {
            double seconds = (ready - now) / 1000.0;
            player.sendMessage(plugin.color(plugin.getConfig().getString("settings.cooldown-message")
                    .replace("%time%", String.format(java.util.Locale.US, "%.1f", seconds))));
            return;
        }
        if (item.getCooldown() > 0) cooldowns.put(key, now + item.getCooldown() * 1000L);
        if ("LIGHTNING".equalsIgnoreCase(item.getAbilityType())) {
            player.getWorld().strikeLightningEffect(player.getTargetBlock((java.util.HashSet<Material>) null, 30).getLocation());
        } else if ("HEAL".equalsIgnoreCase(item.getAbilityType())) {
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 4.0));
        } else if ("SPEED".equalsIgnoreCase(item.getAbilityType())) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 20 * 5, 1));
        }
        if (!item.getParticle().isEmpty()) {
            try { player.getWorld().playEffect(player.getLocation(), Effect.valueOf(item.getParticle()), 0); }
            catch (IllegalArgumentException ignored) { }
        }
        if (!item.getSound().isEmpty()) {
            try { player.playSound(player.getLocation(), Sound.valueOf(item.getSound()), 1F, 1F); }
            catch (IllegalArgumentException ignored) { }
        }
    }
}
