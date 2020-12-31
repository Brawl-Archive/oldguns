package com.brawl.oldguns.listeners;

import com.brawl.oldguns.OldGuns;
import com.brawl.oldguns.events.BulletCollideEvent;
import com.brawl.oldguns.events.GunDamageEntityEvent;
import com.brawl.oldguns.events.GunKillEntityEvent;
import com.brawl.oldguns.gun.Bullet;
import com.brawl.oldguns.gun.Gun;
import com.brawl.oldguns.gun.GunPlayer;
import com.brawl.shared.util.Duration;
import lombok.Data;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PluginEntityListener implements Listener {
    public Map<UUID, PStat> stackedDamage = new HashMap<>();
    OldGuns plugin;

    public PluginEntityListener(OldGuns plugin) {
        this.plugin = plugin;
    }

    private void clearStackedDmg(UUID uuid) {
        stackedDamage.remove(uuid);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
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
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile check = event.getEntity();
        Bullet bullet = OldGuns.getInstance().getBullet(check);
        if (bullet != null) {
            bullet.onHit();
            if (bullet.isDestroyWhenHit())
                bullet.setDestroyNextTick(true);
            Projectile p = event.getEntity();
            Block b = p.getLocation().getBlock();
            int id = b.getTypeId();
            /*if (b == null || id == 0) {
            	for (GunPlayer player : PVPGunPlus.getInstance().getGunPlayers()) {
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

            BulletCollideEvent evv = new BulletCollideEvent(bullet.getShooter(), bullet.getShotFrom(), b);
            evv.callEvent();

            if (bullet.isDestroyWhenHit()) {
                bullet.remove();
                event.getEntity().remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity dead = event.getEntity();
        if (dead.getLastDamageCause() != null) {
            EntityDamageEvent e = dead.getLastDamageCause();
            if (e instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent) e;
                Entity damager = ede.getDamager();
                if (damager instanceof Projectile) {
                    Projectile proj = (Projectile) (damager);
                    Bullet bullet = OldGuns.getInstance().getBullet(proj);
                    if (bullet != null) {
                        Gun used = bullet.getShotFrom();
                        GunPlayer shooter = bullet.getShooter();

                        GunKillEntityEvent pvpgunkill = new GunKillEntityEvent(shooter, used, dead);
                        pvpgunkill.callEvent();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(final EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity))
            return;
        DamageCause cause = event.getCause();

        if (cause == DamageCause.VOID)
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> OldGuns.resetPlayerDamage((LivingEntity) entity, 0), 1);

        if (!event.isCancelled() && (cause == DamageCause.FIRE || cause == DamageCause.LAVA || cause == DamageCause.FIRE_TICK)) {
            event.setCancelled(true);

            PStat stat;

            if (stackedDamage.containsKey(entity.getUniqueId()))
                stat = stackedDamage.get(entity.getUniqueId());
            else {
                stat = new PStat();
                stackedDamage.put(entity.getUniqueId(), stat);
            }

            if (PStat.DELAY_HIT.toMilliseconds() > System.currentTimeMillis() - stat.lastHit)
                return;

            stat.lent = (LivingEntity) entity;
            stat.stackedDamage.add(event.getFinalDamage());
            stat.lastHit = System.currentTimeMillis();
            return;
        }

        if (cause != DamageCause.PROJECTILE && cause != DamageCause.FALL) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> OldGuns.resetPlayerDamage((LivingEntity) entity, 20), 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled())
            return;
        Entity damager = event.getDamager();
        boolean isBullet = false;

        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity hurt = (LivingEntity) event.getEntity();
            if (damager instanceof Projectile) {
                Projectile proj = (Projectile) (damager);
                Bullet bullet = OldGuns.getInstance().getBullet(proj);
                if (bullet != null) {
                    boolean headshot = false;
                    if (isNear(proj.getLocation(), hurt.getEyeLocation(), 0.26D) && bullet.getShotFrom().isCanHeadshot()) {
                        headshot = true;
                    }
                    GunDamageEntityEvent pvpgundmg = new GunDamageEntityEvent(event, bullet.getShooter(), bullet.getShotFrom(), event.getEntity(), headshot);
                    pvpgundmg.callEvent();
                    if (!pvpgundmg.isCancelled() /*&& !(hurt instanceof Player && ((Player) hurt).isInvulnerable())*/) {
                        OldGuns.resetPlayerDamage(hurt, 0);
                        double damage = pvpgundmg.getDamage();
                        double mult = 1.0D;
                        if (pvpgundmg.isHeadshot()) {
                            OldGuns.playEffect(Effect.ZOMBIE_DESTROY_DOOR, hurt.getLocation(), 3);
                            mult = 2D;
                        }

                        hurt.setLastDamage(0.0);

                        event.setDamage(damage * mult);
                        //System.out.println(event.getDamage());
                        int armorPenetration = pvpgundmg.getArmorPenetration();
                        if (armorPenetration > 0) {
                            int health = (int) hurt.getHealth();
                            int newHealth = health - armorPenetration;
                            if (newHealth < 0)
                                newHealth = 0;
                            if (newHealth > 20)
                                newHealth = 20;
                            hurt.setHealth(newHealth);
                        }

                        bullet.getShotFrom().doKnockback(hurt, bullet.getVelocity());

                        if (bullet.isDestroyWhenHit())
                            bullet.remove();

                        if (bullet.getBulletType().equals("crossbow")) {
                            bullet.setStuckTo(hurt);
                            //System.out.println("STUCK GRENADE");
                        }


                        OldGuns.resetPlayerDamage(hurt, 0);
                        isBullet = true;
                    } else {
                        event.setCancelled(true);
                    }
                }
            }
            if (!isBullet) {
                OldGuns.resetPlayerDamage(hurt, 20);
            }
        }
    }

    private boolean isNear(Location location, Location eyeLocation, double d) {
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