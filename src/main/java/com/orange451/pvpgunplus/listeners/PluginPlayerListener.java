package com.orange451.pvpgunplus.listeners;

import java.util.ArrayList;

import com.brawl.base.BrawlPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.evogames.chat.C;
import com.evogames.server.ServerType;
import com.orange451.pvpgunplus.PVPGunPlus;
import com.orange451.pvpgunplus.PermissionInterface;
import com.orange451.pvpgunplus.gun.Gun;
import com.orange451.pvpgunplus.gun.GunPlayer;

public class PluginPlayerListener implements Listener
{
    private PVPGunPlus plugin;

    public PluginPlayerListener(PVPGunPlus plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        plugin.onJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        plugin.onQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        Item dropped = event.getItemDrop();
        Player dropper = event.getPlayer();
        GunPlayer gp = plugin.getGunPlayer(dropper);
        if (gp != null)
        {
            ItemStack lastHold = gp.getLastItemHeld();
            if (lastHold != null)
            {
                Gun gun = gp.getGun(dropped.getItemStack().getTypeId());
                if (gun != null && lastHold.equals(dropped.getItemStack()) && gun.hasClip && gun.changed && gun.reloadGunOnDrop)
                {
                    gun.reloadGun();
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Action action = event.getAction();
        Player player = event.getPlayer();
        ItemStack itm1 = player.getItemInHand();
        if (itm1 != null)
        {
            if ((action.equals(Action.LEFT_CLICK_AIR)) || (action.equals(Action.LEFT_CLICK_BLOCK)) || (action.equals(Action.RIGHT_CLICK_AIR)) || (action.equals(Action.RIGHT_CLICK_BLOCK)))
            {
                String clickType = "left";
                if ((action.equals(Action.RIGHT_CLICK_AIR)) || (action.equals(Action.RIGHT_CLICK_BLOCK)))
                    clickType = "right";
                GunPlayer gp = this.plugin.getGunPlayer(player);
                if (gp != null)
                {
                    gp.onClick(clickType, null);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerShootBow(EntityShootBowEvent event)
    {
        LivingEntity living = event.getEntity();
        if (living instanceof Player)
        {
            GunPlayer gp = this.plugin.getGunPlayer((Player) living);
            if (gp != null)
            {
                gp.onClick("bow", (Projectile) event.getProjectile());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
    	if (!(event.getEntity() instanceof Player))
    		return;
    	
    	GunPlayer gp = PVPGunPlus.getPlugin().getGunPlayer((Player) event.getEntity());
    	if (gp != null && event.getCause() == DamageCause.LAVA)
    		gp.damageByLava(event);
    	
    	if (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK) {
    		Material type = event.getEntity().getLocation().getBlock().getType();
    		
    		if (type == Material.LAVA || type == Material.STATIONARY_LAVA)
    			event.setCancelled(true);
    	}
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        String[] split = event.getMessage().split(" ");
        split[0] = split[0].substring(1);
        String label = split[0];
        String[] args = new String[split.length - 1];
        for (int i = 1; i < split.length; i++)
        {
            args[(i - 1)] = split[i];
        }

        if (label.equalsIgnoreCase("pvpgunplus") && args.length == 0)
        {
            player.sendMessage(ChatColor.DARK_GRAY + "----" + ChatColor.GRAY + "[" + ChatColor.YELLOW + "PVPGUNPLUS" + ChatColor.GRAY + "]" + ChatColor.DARK_GRAY + "----");
            player.sendMessage(ChatColor.GRAY + "/pvpgunplus " + ChatColor.GREEN + "reload" + ChatColor.WHITE + " to reload the server");
            player.sendMessage(ChatColor.GRAY + "/pvpgunplus " + ChatColor.GREEN + "list" + ChatColor.WHITE + " to list the guns loaded into the server");
            player.sendMessage(ChatColor.GRAY + "/pvpgunplus " + ChatColor.GREEN + "toggle" + ChatColor.WHITE + " to toggle whether or not you can fire");
            player.sendMessage(ChatColor.GRAY + "/pvpgunplus " + ChatColor.GREEN + "edit [stat] [amount]" + ChatColor.WHITE + " to edit a gun in your hand");
        }

        try
        {
            if ((label.equalsIgnoreCase("pvpgunplus")) && (args[0].equals("reload")))
            {
                if (player.isOp())
                {
                    this.plugin.reload(true);
                    player.sendMessage("RELOADED PVPGUN");
                }
            }
            
            if ((label.equalsIgnoreCase("pvpgunplus")) && (args[0].equals("edit")))
            {
                if (!player.isOp() || BrawlPlugin.getInstance().getServerType() != ServerType.TEST) {
                	player.sendMessage(C.cmdFail() + "No permissions");
                	return;
                }

                int id = player.getItemInHand().getTypeId();
                Gun g = this.plugin.getGun(id);
                
                player.sendMessage(C.cmdSuccess() + "Editing gun: " + g.getName());
                
                switch(args[1]) {
                case "accuracy":
                	g.setAccuracy(Double.parseDouble(args[2]));
                	break;
                case "accuracyAimed":
                	g.setAccuracyAimed(Double.parseDouble(args[2]));
                	break;
                case "accuracyCrouched":
                	g.setAccuracyCrouched(Double.parseDouble(args[2]));
                	break;
                case "armorPenetration":
                	g.setArmorPenetration(Integer.parseInt(args[2]));
                	break;
                case "bulletSpeed":
                	g.setBulletSpeed(Double.parseDouble(args[2]));
                	break;
                case "bulletsPerClick":
                	g.setBulletsPerClick(Integer.parseInt(args[2]));
                	break;
                case "gunDamage":
                	g.setGunDamage(Integer.parseInt(args[2]));
                	break;
                case "gunType":
                	g.setGunType(args[2]);
                	break;
                case "knockback":
                	g.setKnockback(Double.parseDouble(args[2]));
                	break;
                case "recoil":
                	g.setRecoil(Double.parseDouble(args[2]));
                	break;
                case "reloadTime":
                	g.setReloadTime(Integer.parseInt(args[2]));
                	break;
                case "roundsPerBurst":
                	g.setRoundsPerBurst(Integer.parseInt(args[2]));
                	break;
                case "maxDistance":
                	g.setMaxDistance(Integer.parseInt(args[2]));
                	break;
                case "bulletDelayTime":
                	g.setBulletDelayTime(Integer.parseInt(args[2]));
                	break;
                default:
                	player.sendMessage(C.cmdFail() + "Stat not found, must be: accuracy, accuracyAimed, accuracyCrouched, "
                    		+ "armorPenetration, bulletSpeed, bulletsPerClick, gunDamage, gunType, knockback, recoil, reloadTime, "
                    		+ "roundsPerBurst, maxDistance, bulletDelayTime");
                	return;
                }
                PVPGunPlus.plugin.editLoadedGun(id, g);
                //plugin.editLoadedGun(id, g);
                                
                player.sendMessage(C.cmdSuccess() + "Edited gun!");
            }

            if ((label.equalsIgnoreCase("pvpgunplus")) && (args[0].equals("toggle")))
            {
                if (PermissionInterface.checkPermission(player, "pvpgunplus.user"))
                {
                    GunPlayer gp = plugin.getGunPlayer(player);
                    if (gp != null)
                    {
                        gp.enabled = !gp.enabled;
                        String on = ChatColor.GREEN + "ON";
                        String off = ChatColor.RED + "OFF";
                        if (gp.enabled)
                            player.sendMessage(ChatColor.GRAY + "You have turned guns " + on);
                        else
                            player.sendMessage(ChatColor.GRAY + "You have turned guns " + off);
                    }
                }
            }

            if ((label.equalsIgnoreCase("pvpgunplus")) && (args[0].equals("list")))
            {
                player.sendMessage("-------PVPGUNS-------");

                ArrayList<Gun> loadedGuns = plugin.getLoadedGuns();

                for (int i = 0; i < loadedGuns.size(); i++)
                {
                    Gun g = (Gun) loadedGuns.get(i);
                    player.sendMessage(" -" + g.getName() + ChatColor.YELLOW + "(" + Integer.toString(g.getGunType()) + ")" + ChatColor.GRAY + " AMMO: " + ChatColor.RED + g.getAmmoMaterial().toString() + ChatColor.GRAY + "  amt# " + ChatColor.RED + Integer.toString(g.getAmmoAmtNeeded()));
                }

                player.sendMessage("---------------------");
            }
        } catch (Exception e)
        {
            //
        }
    }
}