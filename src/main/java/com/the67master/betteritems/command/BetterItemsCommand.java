package com.the67master.betteritems.command;

import com.the67master.betteritems.BetterItems;
import com.the67master.betteritems.item.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class BetterItemsCommand implements CommandExecutor, TabCompleter {
    private final BetterItems plugin;
    public BetterItemsCommand(BetterItems plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(plugin.color("&b&lBetterItems &7- Comandos"));
            sender.sendMessage(plugin.color("&e/bi list &7- Lista los items"));
            sender.sendMessage(plugin.color("&e/bi give <jugador> <item> [cantidad]"));
            sender.sendMessage(plugin.color("&e/bi gui &7- Abre el menu grafico (opcional)"));
            sender.sendMessage(plugin.color("&e/bi editor &7- Alias de /bi gui"));
            sender.sendMessage(plugin.color("&e/bi reload &7- Recarga configuraciones"));
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            if (plugin.getItemManager().getItems().isEmpty()) sender.sendMessage(plugin.color("&cNo hay items configurados."));
            for (CustomItem item : plugin.getItemManager().getItems()) sender.sendMessage(plugin.color("&e- &f" + item.getId()));
            return true;
        }
        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("betteritems.admin")) { sender.sendMessage(plugin.message("no-permission")); return true; }
            if (args.length < 3) { sender.sendMessage(plugin.color("&cUso: /bi give <jugador> <item> [cantidad]")); return true; }
            Player target = Bukkit.getPlayerExact(args[1]);
            CustomItem item = plugin.getItemManager().getItem(args[2]);
            if (target == null) { sender.sendMessage(plugin.color("&cEse jugador no está conectado.")); return true; }
            if (item == null) { sender.sendMessage(plugin.message("unknown-item").replace("%item%", args[2])); return true; }
            int amount = 1;
            if (args.length >= 4) {
                try { amount = Math.max(1, Math.min(64, Integer.parseInt(args[3]))); }
                catch (NumberFormatException ex) { sender.sendMessage(plugin.color("&cCantidad inválida.")); return true; }
            }
            ItemStack stack = item.getItemStack();
            stack.setAmount(amount);
            target.getInventory().addItem(stack);
            sender.sendMessage(plugin.color("&aEntregaste &e" + amount + "x " + item.getId() + " &aa " + target.getName() + "."));
            return true;
        }
        if (args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("editor")) {
            if (!(sender instanceof Player)) { sender.sendMessage("Este comando solo puede usarlo un jugador."); return true; }
            if (!sender.hasPermission("betteritems.admin")) { sender.sendMessage(plugin.message("no-permission")); return true; }
            openEditor((Player) sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("betteritems.admin")) { sender.sendMessage(plugin.message("no-permission")); return true; }
            plugin.reloadConfig();
            plugin.reloadItemsConfig();
            plugin.getItemManager().loadItems();
            new com.the67master.betteritems.recipe.RecipeManager(plugin).registerAll();
            sender.sendMessage(plugin.color("&aBetterItems recargado. Items: &e" + plugin.getItemManager().getItems().size()));
            return true;
        }
        sender.sendMessage(plugin.color("&cSubcomando desconocido. Usa /bi help."));
        return true;
    }

    private void openEditor(Player player) {
        int size = Math.max(9, Math.min(54, ((plugin.getItemManager().getItems().size() / 9) + 1) * 9));
        Inventory inventory = Bukkit.createInventory(null, size, plugin.color("&8BetterItems &7- &fItems"));
        for (CustomItem item : plugin.getItemManager().getItems()) inventory.addItem(item.getItemStack());
        player.openInventory(inventory);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<String>();
        if (args.length == 1) {
            for (String value : new String[]{"help", "list", "give", "gui", "editor", "reload"}) if (value.startsWith(args[0].toLowerCase())) options.add(value);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Player player : Bukkit.getOnlinePlayers()) if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) options.add(player.getName());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (CustomItem item : plugin.getItemManager().getItems()) if (item.getId().toLowerCase().startsWith(args[2].toLowerCase())) options.add(item.getId());
        }
        return options;
    }
}
