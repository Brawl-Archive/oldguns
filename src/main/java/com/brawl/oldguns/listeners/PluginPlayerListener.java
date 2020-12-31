package com.brawl.oldguns.listeners;

import com.brawl.oldguns.OldGuns;
import com.brawl.oldguns.gun.Gun;
import com.brawl.oldguns.gun.GunPlayer;
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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PluginPlayerListener implements Listener {
    private final OldGuns plugin;

    public PluginPlayerListener(OldGuns plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!GunPlayer.meta.has(event.getPlayer())) {
            GunPlayer gp = new GunPlayer(OldGuns.getInstance(), event.getPlayer());
            GunPlayer.meta.put(event.getPlayer(), gp);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (GunPlayer.meta.has(event.getPlayer())) {
            GunPlayer.meta.remove(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item dropped = event.getItemDrop();
        Player dropper = event.getPlayer();
        GunPlayer gp = plugin.getGunPlayer(dropper);
        if (gp != null) {
            ItemStack lastHold = gp.getLastHeldItem();
            if (lastHold != null) {
                Gun gun = gp.getGun(dropped.getItemStack().getTypeId());
                if (gun != null && lastHold.equals(dropped.getItemStack()) && gun.isHasClip() && gun.isChanged() && gun.isReloadGunOnDrop()) {
                    gun.reloadGun();
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player = event.getPlayer();
        ItemStack itm1 = player.getItemInHand();
        if (itm1 != null) {
            if ((action.equals(Action.LEFT_CLICK_AIR)) || (action.equals(Action.LEFT_CLICK_BLOCK)) || (action.equals(Action.RIGHT_CLICK_AIR)) || (action.equals(Action.RIGHT_CLICK_BLOCK))) {
                String clickType = "left";
                if ((action.equals(Action.RIGHT_CLICK_AIR)) || (action.equals(Action.RIGHT_CLICK_BLOCK)))
                    clickType = "right";
                GunPlayer gp = this.plugin.getGunPlayer(player);
                if (gp != null) {
                    gp.onClick(clickType, null);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerShootBow(EntityShootBowEvent event) {
        LivingEntity living = event.getEntity();
        if (living instanceof Player) {
            GunPlayer gp = this.plugin.getGunPlayer((Player) living);
            if (gp != null) {
                gp.onClick("bow", (Projectile) event.getProjectile());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        GunPlayer gp = OldGuns.getInstance().getGunPlayer((Player) event.getEntity());
        if (gp != null && event.getCause() == DamageCause.LAVA)
            gp.damageByLava(event);

        if (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK) {
            Material type = event.getEntity().getLocation().getBlock().getType();

            if (type == Material.LAVA || type == Material.STATIONARY_LAVA)
                event.setCancelled(true);
        }
    }
}