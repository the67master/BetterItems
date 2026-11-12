package com.the67master.betteritems.listener;

import com.the67master.betteritems.BetterItems;
import com.the67master.betteritems.item.CustomItem;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;
import java.util.*;

public class AbilityListener implements Listener {
    private final BetterItems plugin;
    private final Map<String,Long> cooldowns=new HashMap<String,Long>();
    private static final Set<String> PASSIVE_TYPES=new HashSet<String>(Arrays.asList("HELD","ARMOR","ANY"));

    public AbilityListener(BetterItems plugin){
        this.plugin=plugin;
        Bukkit.getScheduler().runTaskTimer(plugin,new Runnable(){public void run(){applyPassiveEffects();}},20L,20L);
    }

    @EventHandler public void onInteract(PlayerInteractEvent e){
        Action a=e.getAction();
        String trigger=(a==Action.RIGHT_CLICK_AIR||a==Action.RIGHT_CLICK_BLOCK)?"RIGHT_CLICK":
                (a==Action.LEFT_CLICK_AIR||a==Action.LEFT_CLICK_BLOCK)?"LEFT_CLICK":"";
        if(trigger.isEmpty())return;
        runForItem(e.getPlayer(),e.getItem(),trigger,null);
    }

    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=true)
    public void onDamage(EntityDamageByEntityEvent e){
        if(!(e.getDamager() instanceof Player))return;
        Player attacker=(Player)e.getDamager();
        CustomItem item=plugin.getItemManager().identify(attacker.getItemInHand());
        if(item==null)return;
        for(ConfigurationSection ability:getAbilities(item)){
            String trigger=ability.getString("trigger","HIT");
            if(!trigger.equalsIgnoreCase("HIT"))continue;
            String type=ability.getString("type","NONE").toUpperCase(Locale.ROOT);
            if(type.equals("CRITICAL_ALL")||type.equals("CRITICAL_HIT")){
                double multiplier=ability.getDouble("multiplier",1.5D);
                e.setDamage(e.getDamage()*Math.max(1.0D,multiplier));
                attacker.getWorld().playEffect(e.getEntity().getLocation(),Effect.CRIT,0);
                playSound(attacker,ability.getString("sound","ENTITY_PLAYER_ATTACK_CRIT"));
                spawnParticles(e.getEntity().getLocation(),ability);
            }else if(type.equals("POTION_ON_HIT")&&e.getEntity() instanceof LivingEntity){
                applyPotion((LivingEntity)e.getEntity(),ability);
            }else{
                executeAbility(attacker,e.getEntity(),ability);
            }
        }
    }

    private void runForItem(Player player,ItemStack stack,String trigger,Entity target){
        CustomItem item=plugin.getItemManager().identify(stack);
        if(item==null)return;
        for(ConfigurationSection ability:getAbilities(item)){
            if(!ability.getString("trigger","RIGHT_CLICK").equalsIgnoreCase(trigger))continue;
            if(!allowed(player,item,ability))continue;
            int seconds=ability.getInt("cooldown",item.getCooldown());
            String key=player.getUniqueId()+":"+item.getId()+":"+ability.getName();
            long now=System.currentTimeMillis(),ready=cooldowns.containsKey(key)?cooldowns.get(key):0L;
            if(ready>now){sendCooldown(player,(ready-now)/1000.0);continue;}
            executeAbility(player,target,ability);
            if(seconds>0){
                cooldowns.put(key,now+seconds*1000L);
                int bar=ability.getInt("cooldown-bar-ticks",item.getCooldownBarTicks());
                if(bar>0)player.setCooldown(item.getMaterial(),bar);
            }
        }
    }

    private boolean allowed(Player p,CustomItem item,ConfigurationSection a){
        String permission=a.getString("permission",item.getPermission());
        if(!p.hasPermission("betteritems.use")||(!permission.isEmpty()&&!p.hasPermission(permission))){
            p.sendMessage(plugin.color("&cNo tienes permiso para usar esta habilidad."));return false;
        }
        return true;
    }

    private void executeAbility(Player p,Entity target,ConfigurationSection a){
        String type=a.getString("type","NONE").toUpperCase(Locale.ROOT);
        Location loc=target==null?p.getTargetBlock((HashSet<Material>)null,Math.max(1,a.getInt("range",25))).getLocation():target.getLocation();
        World w=p.getWorld();
        double amount=a.getDouble("amount",4.0D);
        int duration=Math.max(1,a.getInt("duration",5))*20;
        int level=Math.max(0,a.getInt("level",1)-1);
        switch(type){
            case "COMMAND": case "RUN_COMMAND":
                for(String command:a.getStringList("commands"))Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command.replace("{player}",p.getName()).replace("{uuid}",p.getUniqueId().toString()).replace("{world}",p.getWorld().getName()).replaceFirst("^/",""));
                if(a.isString("command"))Bukkit.dispatchCommand(Bukkit.getConsoleSender(),a.getString("command").replace("{player}",p.getName()).replace("{uuid}",p.getUniqueId().toString()).replaceFirst("^/",""));
                break;
            case "LIGHTNING": case "LIGHTNING_EFFECT": w.strikeLightningEffect(loc);break;
            case "LIGHTNING_DAMAGE": w.strikeLightning(loc);break;
            case "HEAL": p.setHealth(Math.min(p.getMaxHealth(),p.getHealth()+amount));break;
            case "FEED": p.setFoodLevel(Math.min(20,p.getFoodLevel()+(int)amount));p.setSaturation(5F);break;
            case "SATURATION": potion(p,PotionEffectType.SATURATION,duration,level);break;
            case "SPEED": potion(p,PotionEffectType.SPEED,duration,level);break;
            case "SLOWNESS": potion(p,PotionEffectType.SLOW,duration,level);break;
            case "JUMP": potion(p,PotionEffectType.JUMP,duration,level);break;
            case "STRENGTH": potion(p,PotionEffectType.INCREASE_DAMAGE,duration,level);break;
            case "RESISTANCE": potion(p,PotionEffectType.DAMAGE_RESISTANCE,duration,level);break;
            case "FIRE_RESISTANCE": potion(p,PotionEffectType.FIRE_RESISTANCE,duration,level);break;
            case "WATER_BREATHING": potion(p,PotionEffectType.WATER_BREATHING,duration,level);break;
            case "NIGHT_VISION": potion(p,PotionEffectType.NIGHT_VISION,duration,level);break;
            case "INVISIBILITY": potion(p,PotionEffectType.INVISIBILITY,duration,level);break;
            case "REGENERATION": potion(p,PotionEffectType.REGENERATION,duration,level);break;
            case "ABSORPTION": potion(p,PotionEffectType.ABSORPTION,duration,level);break;
            case "DAMAGE": if(target instanceof LivingEntity)((LivingEntity)target).damage(amount,p);break;
            case "KNOCKBACK": if(target!=null)target.setVelocity(p.getLocation().getDirection().multiply(a.getDouble("power",1.5D)));break;
            case "LAUNCH": p.setVelocity(p.getLocation().getDirection().multiply(a.getDouble("power",2.0D)));break;
            case "TELEPORT_FORWARD": p.teleport(p.getLocation().add(p.getLocation().getDirection().multiply(a.getDouble("distance",5))));break;
            case "EXPLOSION_EFFECT": w.createExplosion(loc,0F);break;
            case "FIREBALL": p.launchProjectile(Fireball.class);break;
            case "SNOWBALL": p.launchProjectile(Snowball.class);break;
            case "ENDER_PEARL": p.launchProjectile(EnderPearl.class);break;
            case "ARROW": p.launchProjectile(Arrow.class);break;
            case "POISON": potionTarget(target,PotionEffectType.POISON,duration,level);break;
            case "WITHER": potionTarget(target,PotionEffectType.WITHER,duration,level);break;
            case "BLINDNESS": potionTarget(target,PotionEffectType.BLINDNESS,duration,level);break;
            case "WEAKNESS": potionTarget(target,PotionEffectType.WEAKNESS,duration,level);break;
            case "SLOW_TARGET": potionTarget(target,PotionEffectType.SLOW,duration,level);break;
            case "POTION_ON_HIT": applyPotionTarget(target,a);break;
            case "FIRE_TARGET": if(target!=null)target.setFireTicks(a.getInt("fire-ticks",100));break;
            case "EXTINGUISH": p.setFireTicks(0);break;
            case "CLEANSE": for(PotionEffect effect:p.getActivePotionEffects())p.removePotionEffect(effect.getType());break;
            case "CURE_POISON": p.removePotionEffect(PotionEffectType.POISON);p.removePotionEffect(PotionEffectType.WITHER);break;
            case "CRITICAL_ALL": case "CRITICAL_HIT": p.getWorld().playEffect(p.getLocation(),Effect.CRIT,0);break;
            case "PARTICLE_BURST": spawnParticles(loc,a);break;
            case "SOUND": playSound(p,a.getString("sound","ENTITY_EXPERIENCE_ORB_PICKUP"));break;
            case "PULL_NEARBY": for(Entity ent:w.getNearbyEntities(p.getLocation(),a.getDouble("radius",5),a.getDouble("radius",5),a.getDouble("radius",5)))if(ent!=p)ent.setVelocity(p.getLocation().toVector().subtract(ent.getLocation().toVector()).normalize().multiply(0.7));break;
            case "PUSH_NEARBY": for(Entity ent:w.getNearbyEntities(p.getLocation(),a.getDouble("radius",5),a.getDouble("radius",5),a.getDouble("radius",5)))if(ent!=p)ent.setVelocity(ent.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.2));break;
            case "AREA_HEAL": for(Entity ent:w.getNearbyEntities(p.getLocation(),a.getDouble("radius",5),a.getDouble("radius",5),a.getDouble("radius",5)))if(ent instanceof Player)((Player)ent).setHealth(Math.min(((Player)ent).getMaxHealth(),((Player)ent).getHealth()+amount));break;
            case "AREA_DAMAGE": for(Entity ent:w.getNearbyEntities(p.getLocation(),a.getDouble("radius",5),a.getDouble("radius",5),a.getDouble("radius",5)))if(ent instanceof LivingEntity&&ent!=p)((LivingEntity)ent).damage(amount,p);break;
            case "DOLPHINS_GRACE": potion(p,PotionEffectType.SPEED,duration,level);break;
            case "HASTE": potion(p,PotionEffectType.FAST_DIGGING,duration,level);break;
            case "MINING_FATIGUE": potion(p,PotionEffectType.SLOW_DIGGING,duration,level);break;
            case "LEVITATION": p.setVelocity(p.getVelocity().setY(a.getDouble("power",0.8D)));break;
            case "NONE": default: break;
        }
        spawnParticles(loc,a);
        String sound=a.getString("sound","");
        if(!sound.isEmpty()&&!type.equals("SOUND"))playSound(p,sound);
    }

    private List<ConfigurationSection> getAbilities(CustomItem item){
        List<ConfigurationSection> list=new ArrayList<ConfigurationSection>();
        ConfigurationSection root=item.getSection().getConfigurationSection("abilities");
        if(root!=null)for(String key:root.getKeys(false)){ConfigurationSection a=root.getConfigurationSection(key);if(a!=null)list.add(a);}
        ConfigurationSection legacy=item.getSection().getConfigurationSection("ability");
        if(root==null&&legacy!=null)list.add(legacy);
        return list;
    }

    private void applyPassiveEffects(){
        for(Player p:Bukkit.getOnlinePlayers()){
            applyPassive(p,p.getItemInHand(),"HELD");
            for(ItemStack armor:p.getInventory().getArmorContents())applyPassive(p,armor,"ARMOR");
            for(ItemStack stack:p.getInventory().getContents())if(stack!=null&&stack.getType()!=Material.AIR)applyPassive(p,stack,"ANY");
        }
    }
    private void applyPassive(Player p,ItemStack stack,String mode){
        CustomItem item=plugin.getItemManager().identify(stack);if(item==null)return;
        ConfigurationSection effects=item.getSection().getConfigurationSection("passive-effects");if(effects==null)return;
        for(String key:effects.getKeys(false)){
            ConfigurationSection effect=effects.getConfigurationSection(key);if(effect==null)continue;
            String applies=effect.getString("while","ANY").toUpperCase(Locale.ROOT);
            if(!applies.equals(mode)&&!applies.equals("ANY"))continue;
            String perm=effect.getString("permission",item.getPermission());
            if(!p.hasPermission("betteritems.use")||(!perm.isEmpty()&&!p.hasPermission(perm)))continue;
            PotionEffectType potion=PotionEffectType.getByName(effect.getString("effect","SPEED").toUpperCase(Locale.ROOT));
            if(potion!=null)p.addPotionEffect(new PotionEffect(potion,Math.max(40,effect.getInt("duration",60)),Math.max(0,effect.getInt("level",1)-1),true,false),true);
        }
    }
    private void applyPotion(LivingEntity target,ConfigurationSection a){applyPotionTarget(target,a);}
    private void applyPotionTarget(Entity target,ConfigurationSection a){
        if(!(target instanceof LivingEntity))return;
        PotionEffectType type=PotionEffectType.getByName(a.getString("effect","POISON").toUpperCase(Locale.ROOT));
        if(type!=null)((LivingEntity)target).addPotionEffect(new PotionEffect(type,Math.max(1,a.getInt("duration",5))*20,Math.max(0,a.getInt("level",1)-1)));
    }
    private void potionTarget(Entity target,PotionEffectType type,int duration,int level){if(target instanceof LivingEntity)((LivingEntity)target).addPotionEffect(new PotionEffect(type,duration,level));}
    private void potion(Player p,PotionEffectType type,int duration,int level){p.addPotionEffect(new PotionEffect(type,duration,level));}
    private void playSound(Player p,String name){try{p.playSound(p.getLocation(),Sound.valueOf(name.toUpperCase(Locale.ROOT)),1F,1F);}catch(Exception ignored){}}
    private void spawnParticles(Location loc,ConfigurationSection a){
        String name=a.getString("particle","");if(name.isEmpty())return;
        try{
            Particle particle=Particle.valueOf(name.toUpperCase(Locale.ROOT));
            int count=Math.max(1,a.getInt("particle-count",12));
            loc.getWorld().spawnParticle(particle,loc.clone().add(0,1,0),count,
                    a.getDouble("offset-x",0.35),a.getDouble("offset-y",0.5),a.getDouble("offset-z",0.35),a.getDouble("particle-speed",0.05));
        }catch(Exception ignored){}
        if(a.contains("particle-color")){
            String[] rgb=a.getString("particle-color","255,255,255").split(",");
            if(rgb.length==3)try{
                float r=Math.max(0,Math.min(1,Integer.parseInt(rgb[0].trim())/255F));
                float g=Math.max(0,Math.min(1,Integer.parseInt(rgb[1].trim())/255F));
                float b=Math.max(0,Math.min(1,Integer.parseInt(rgb[2].trim())/255F));
                loc.getWorld().spigot().playEffect(loc,Effect.COLOURED_DUST,0,0,r,g,b,1F,0,64);
            }catch(Exception ignored){}
        }
    }
    private void sendCooldown(Player p,double seconds){
        p.sendMessage(plugin.color("&cEspera &e"+String.format(Locale.US,"%.1f",seconds)+"s &cpara volver a usarlo."));
    }
}
