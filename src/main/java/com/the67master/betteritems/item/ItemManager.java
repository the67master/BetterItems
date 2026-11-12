package com.the67master.betteritems.item;

import com.the67master.betteritems.BetterItems;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public class ItemManager {
    private final BetterItems plugin;
    private final Map<String,CustomItem> items=new LinkedHashMap<String,CustomItem>();
    public ItemManager(BetterItems plugin){this.plugin=plugin;}

    public void loadItems(){
        items.clear();
        ConfigurationSection root=plugin.getConfigFromItems().getConfigurationSection("items");
        if(root==null)return;
        for(String id:root.getKeys(false)){
            ConfigurationSection s=root.getConfigurationSection(id);
            if(s==null)continue;
            Material material=Material.matchMaterial(s.getString("material","STONE"));
            if(material==null){plugin.getLogger().warning("Invalid material for item: "+id);continue;}
            int amount=Math.max(1,Math.min(64,s.getInt("amount",1)));
            ItemStack stack=new ItemStack(material,amount);
            ItemMeta meta=stack.getItemMeta();
            if(s.contains("name"))meta.setDisplayName(plugin.color(s.getString("name")));
            List<String> lore=new ArrayList<String>();
            for(String line:s.getStringList("lore"))lore.add(plugin.color(line));
            lore.add(ChatColor.BLACK+"betteritems:"+id.toLowerCase(Locale.ROOT));
            meta.setLore(lore);
            meta.spigot().setUnbreakable(s.getBoolean("unbreakable",false));
            if(s.getBoolean("glow",false)){meta.addEnchant(Enchantment.DURABILITY,1,true);meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);}
            for(String flag:s.getStringList("hide-flags")){
                try{meta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase(Locale.ROOT)));}catch(Exception ignored){}
            }
            stack.setItemMeta(meta);
            items.put(id.toLowerCase(Locale.ROOT),new CustomItem(id,material,amount,s.getString("permission",""),
                    s.getInt("cooldown",0),s.getInt("cooldown-bar-ticks",s.getInt("cooldown",0)*20),stack,s));
        }
    }
    public CustomItem getItem(String id){return id==null?null:items.get(id.toLowerCase(Locale.ROOT));}
    public Collection<CustomItem> getItems(){return Collections.unmodifiableCollection(items.values());}
    public CustomItem identify(ItemStack stack){
        if(stack==null||!stack.hasItemMeta()||!stack.getItemMeta().hasLore())return null;
        for(String line:stack.getItemMeta().getLore()){
            String plain=ChatColor.stripColor(line);
            if(plain!=null&&plain.startsWith("betteritems:"))return getItem(plain.substring("betteritems:".length()));
        }
        return null;
    }
}
