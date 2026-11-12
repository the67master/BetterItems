package com.the67master.betteritems.recipe;

import com.the67master.betteritems.BetterItems;
import com.the67master.betteritems.item.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import java.util.*;

public class RecipeManager {
    private final BetterItems plugin;
    public RecipeManager(BetterItems plugin){this.plugin=plugin;}

    public void registerAll(){
        for(CustomItem item:plugin.getItemManager().getItems()){
            ConfigurationSection recipe=item.getSection().getConfigurationSection("recipe");
            if(recipe==null||!recipe.getBoolean("enabled",false))continue;
            List<String> shape=recipe.getStringList("shape");
            if(shape.isEmpty()||shape.size()>3){plugin.getLogger().warning("Invalid recipe shape for "+item.getId());continue;}
            ShapedRecipe shaped=new ShapedRecipe(new NamespacedKey(plugin,item.getId().toLowerCase(Locale.ROOT)),item.getItemStack());
            shaped.shape(shape.toArray(new String[shape.size()]));
            ConfigurationSection ingredients=recipe.getConfigurationSection("ingredients");
            if(ingredients==null)continue;
            for(String symbol:ingredients.getKeys(false)){
                String materialName=ingredients.getString(symbol,"");
                Material material=Material.matchMaterial(materialName);
                if(material==null||symbol.length()!=1){plugin.getLogger().warning("Invalid recipe ingredient "+symbol+" for "+item.getId());continue;}
                shaped.setIngredient(symbol.charAt(0),material);
            }
            Bukkit.addRecipe(shaped);
        }
    }
}
