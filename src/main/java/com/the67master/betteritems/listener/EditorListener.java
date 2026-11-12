package com.the67master.betteritems.listener;

import com.the67master.betteritems.BetterItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class EditorListener implements Listener {
    private final BetterItems plugin;
    public EditorListener(BetterItems plugin) { this.plugin = plugin; }

    @EventHandler
    public void onEditorClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title != null && title.equals(plugin.color("&8BetterItems &7- &fItems"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEditorDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (title != null && title.equals(plugin.color("&8BetterItems &7- &fItems"))) {
            event.setCancelled(true);
        }
    }
}
