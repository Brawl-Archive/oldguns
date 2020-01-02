package com.orange451.pvpgunplus.listeners;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.brawl.base.util.scheduler.Sync;
import com.evogames.util.Duration;
import com.orange451.pvpgunplus.PVPGunPlus;
import com.orange451.pvpgunplus.events.PVPGunPlusBulletCollideEvent;
import com.orange451.pvpgunplus.events.PVPGunPlusGunDamageEntityEvent;
import com.orange451.pvpgunplus.events.PVPGunPlusGunKillEntityEvent;
import com.orange451.pvpgunplus.gun.Bullet;
import com.orange451.pvpgunplus.gun.Gun;
import com.orange451.pvpgunplus.gun.GunPlayer;

import lombok.Data;
import net.minecraft.server.v1_8_R3.EntityPlayer;

public class PluginEntityListener implements Listener
{
    PVPGunPlus plugin;
    public Map<UUID, PStat> stackedDamage = new HashMap<>();
    
    public PluginEntityListener(PVPGunPlus plugin)
    {
        this.plugin = plugin;
    }

    private void clearStackedDmg(UUID uuid) {
    	stackedDamage.remove(uuid);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        EntityPlayer ep = ((CraftPlayer) event.getEntity()).getHandle();

        ep.maxNoDamageTicks = 20;
        ep.noDamageTicks = 20;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
    	clearStackedDmg(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHit(ProjectileHitEvent event)
    {
        Projectile check = event.getEntity();
        Bullet bullet = PVPGunPlus.getPlugin().getBullet(check);
        if (bullet != null)
        {
            bullet.onHit();
            if (bullet.destroyWhenHit)
                bullet.setNextTickDestroy();
            Projectile p = event.getEntity();
            Block b = p.getLocation().getBlock();
            int id = b.getTypeId();
            /*if (b == null || id == 0) {
            	for (GunPlayer player : PVPGunPlus.getPlugin().getGunPlayers()) {
            		if (player.lastHit <= 0 || player.lastHitLocation == null || player.lastHitSource == null)
            			break;
            		final long HIT_DELAY = 800L;
            		final double DISTANCE_REQUIRED = 0.7D;
            		if (System.currentTimeMillis() - player.lastHit >= HIT_DELAY && player.lastHitLocation.distance(p.getLocation()) <= DISTANCE_REQUIRED &&
            				player.lastHitSource == bullet.getShooter()) {
            			plugin.getServer().getPluginManager().callEvent(new EntityDamageByEntityEvent(check, player.getPlayer(), DamageCause.PROJECTILE, 0D));
            			break;
            		}
            	}
            }*/
            for (double i = 0.2; i < 4; i += 0.2) {
                if (id == 0) {
                    b = p.getLocation().add(p.getVelocity().normalize().multiply(i)).getBlock();
                    id = b.getTypeId();
                }
            }

            if (id > 0) {
                p.getLocation().getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, id);
            }

            PVPGunPlusBulletCollideEvent evv = new PVPGunPlusBulletCollideEvent(bullet.getShooter(), bullet.getGun(), b);
            evv.callEvent();

            if (bullet.destroyWhenHit)
            {
                bullet.remove();
                event.getEntity().remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event)
    {
        Entity dead = event.getEntity();
        if (dead.getLastDamageCause() != null)
        {
            EntityDamageEvent e = dead.getLastDamageCause();
            if (e instanceof EntityDamageByEntityEvent)
            {
                EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent) e;
                Entity damager = ede.getDamager();
                if (damager instanceof Projectile)
                {
                    Projectile proj = (Projectile) (damager);
                    Bullet bullet = PVPGunPlus.getPlugin().getBullet(proj);
                    if (bullet != null)
                    {
                        Gun used = bullet.getGun();
                        GunPlayer shooter = bullet.getShooter();

                        PVPGunPlusGunKillEntityEvent pvpgunkill = new PVPGunPlusGunKillEntityEvent(shooter, used, dead);
                        pvpgunkill.callEvent();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(final EntityDamageEvent event)
    {
    	Entity entity = event.getEntity();
    	if (!(entity instanceof LivingEntity))
    		return;
    	DamageCause cause = event.getCause();
    	
    	if(cause == DamageCause.VOID)
    		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> PVPGunPlus.resetPlayerDamage((LivingEntity) entity, 0), 1);
    	
    	if(!event.isCancelled() && (cause == DamageCause.FIRE || cause == DamageCause.LAVA || cause == DamageCause.FIRE_TICK)) {
    		event.setCancelled(true);
    		
    		PStat stat;
    		
    		if(stackedDamage.containsKey(entity.getUniqueId()))
    			stat = stackedDamage.get(entity.getUniqueId());
    		else {
    			stat = new PStat();
    			stackedDamage.put(entity.getUniqueId(), stat);
    		}
    		
    		if(PStat.DELAY_HIT.toMilliseconds() > System.currentTimeMillis() - stat.lastHit)
    			return;
    		
    		stat.lent = (LivingEntity)entity;
    		stat.stackedDamage.add(event.getFinalDamage());
    		stat.lastHit = System.currentTimeMillis();
    		return;
    	}
    	
    	if (!cause.equals(DamageCause.PROJECTILE)) {
    		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> PVPGunPlus.resetPlayerDamage((LivingEntity) entity, 20), 1);
    	}
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        if (event.isCancelled())
            return;
        Entity damager = event.getDamager();
        boolean isBullet = false;

        if (event.getEntity() instanceof LivingEntity)
        {
            LivingEntity hurt = (LivingEntity) event.getEntity();
            if (damager instanceof Projectile)
            {
                Projectile proj = (Projectile) (damager);
                Bullet bullet = PVPGunPlus.getPlugin().getBullet(proj);
                if (bullet != null)
                {
                    boolean headshot = false;
                    if (isNear(proj.getLocation(), hurt.getEyeLocation(), 0.26D) && bullet.getGun().canHeadShot()) {
                        headshot = true;
                    }
                    PVPGunPlusGunDamageEntityEvent pvpgundmg = new PVPGunPlusGunDamageEntityEvent(event, bullet.getShooter(), bullet.getGun(), event.getEntity(), headshot);
                    pvpgundmg.callEvent();
                    if (!pvpgundmg.isCancelled() /*&& !(hurt instanceof Player && ((Player) hurt).isInvulnerable())*/) {
                    	PVPGunPlus.resetPlayerDamage(hurt, 0);
                        double damage = pvpgundmg.getDamage();
                        double mult = 1.0D;
                        if (pvpgundmg.isHeadshot())
                        {
                            PVPGunPlus.playEffect(Effect.ZOMBIE_DESTROY_DOOR, hurt.getLocation(), 3);
                            mult = 2D;
                        }

                        hurt.setLastDamage(0.0);

                        event.setDamage(damage * mult);
                        //System.out.println(event.getDamage());
                        int armorPenetration = bullet.getGun().getArmorPenetration();
                        if (armorPenetration > 0) {
                            int health = (int) ((Damageable) hurt).getHealth();
                            int newHealth = health - armorPenetration;
                            if (newHealth < 0)
                                newHealth = 0;
                            if (newHealth > 20)
                                newHealth = 20;
                            hurt.setHealth(newHealth);
                        }

                        if (bullet.destroyWhenHit)
                            bullet.remove();

                        if (bullet.bulletType.equals("crossbow"))
                        {
                            bullet.setStuckTo(hurt);
                            //System.out.println("STUCK GRENADE");
                        }


                        PVPGunPlus.resetPlayerDamage(hurt, 0);
                        isBullet = true;
                        
                        Sync.get().delay(1).run(() -> bullet.getGun().doKnockback(hurt, bullet.getVelocity()));
                    } else
                    {
                        event.setCancelled(true);
                    }
                }
            }
            if (!isBullet) {
                PVPGunPlus.resetPlayerDamage(hurt, 20);
            }
        }
    }

    private boolean isNear(Location location, Location eyeLocation, double d)
    {
        return Math.abs(location.getY() - eyeLocation.getY()) <= d;
    }
    
    @Data
	public static class PStat {
    	
    	public static final Duration DELAY_HIT = Duration.ticks(15);
    	
    	private LivingEntity lent;
    	private long lastHit = 0;
    	private ArrayDeque<Double> stackedDamage = new ArrayDeque<>();
    	
    }
}