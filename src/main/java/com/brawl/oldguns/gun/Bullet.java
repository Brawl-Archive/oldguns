package com.brawl.oldguns.gun;

import com.brawl.base.util.scheduler.Sync;
import com.brawl.oldguns.GunTests;
import com.brawl.oldguns.OldGuns;
import com.brawl.oldguns.events.ProjectileDamageEvent;
import com.brawl.oldguns.util.CustomExplosion;
import com.brawl.oldguns.util.RaycastHelper;
import com.brawl.shared.util.Duration;
import com.brawl.shared.util.math.Vec3;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class Bullet {
    private static final Duration FIRE_TIME = Duration.seconds(5);
    private static final int MAX_FLASH = 70;
    private String bulletType = "";
    private boolean destroyWhenHit;
    private int ticks;
    private int releaseTime;
    private boolean dead = false;
    private boolean active = true;
    private boolean destroyNextTick = false;
    private boolean released = false;
    private Entity projectile;
    private Vector velocity;
    private Location lastLocation;
    private Location startLocation;
    private GunPlayer shooter;
    private Gun shotFrom;
    private LivingEntity stuckTo;

    public Bullet(GunPlayer owner, Vector vec, Gun gun, Projectile alreadyFired) {
        shotFrom = gun;
        shooter = owner;
        velocity = vec;

        destroyWhenHit = gun.isDestroyBulletWhenHit();

        if (alreadyFired != null) {
            projectile = alreadyFired;
        }

        if (gun.isThrowable()) {
            ItemStack thrown = new ItemStack(gun.getGunType(), 1, gun.getGunByte());
            projectile = owner.getPlayer().getWorld().dropItem(owner.getPlayer().getEyeLocation(), thrown);
            ((Item) projectile).setPickupDelay(9999999);
        } else {
            Class<? extends Projectile> mclass = Snowball.class;
            String check = gun.getProjType().replace(" ", "").replace("_", "");
            bulletType = check;
            if (check.equalsIgnoreCase("egg"))
                mclass = Egg.class;
            if (check.equalsIgnoreCase("arrow"))
                mclass = Arrow.class;
            if (check.equalsIgnoreCase("wither") || check.equalsIgnoreCase("witherskull"))
                mclass = WitherSkull.class;
            if (check.equalsIgnoreCase("fireball"))
                mclass = Fireball.class;
            if (check.equalsIgnoreCase("smallfireball"))
                mclass = SmallFireball.class;
            if (check.equalsIgnoreCase("largefireball"))
                mclass = LargeFireball.class;
            if (check.equalsIgnoreCase("enderpearl"))
                mclass = EnderPearl.class;
            if (check.equalsIgnoreCase("fish"))
                mclass = Fish.class;
            if (check.equalsIgnoreCase("thrownexpbottle"))
                mclass = ThrownExpBottle.class;
            if (check.equalsIgnoreCase("thrownpotion"))
                mclass = ThrownPotion.class;
            if (check.equalsIgnoreCase("crossbow")) {
                mclass = Arrow.class;
                shotFrom.setExplodeRadius(2.5);
                shotFrom.setExplosionDamage(28);
                destroyWhenHit = false;
            }
            if (check.equalsIgnoreCase("laser")) {
                Location loc = owner.getPlayer().getEyeLocation();
                Block b_hit = RaycastHelper.rayCastToBlockDistance(loc, vec, true).getBlock();
                final Player p_hit = RaycastHelper.rayCastToPlayer(loc, vec);
                Location to = b_hit.getLocation();

                if ((p_hit != null) && (loc.distance(p_hit.getLocation()) < loc.distance(b_hit.getLocation()))) {
                    to = p_hit.getLocation();
                    p_hit.damage(shotFrom.getGunDamage(), shooter.getPlayer());
                }
                projectile = null;
                lastLocation = to;
                explode();
                remove();
                return;
            }

            if (projectile == null)
                projectile = owner.getPlayer().launchProjectile(mclass);
            ((Projectile) projectile).setShooter(owner.getPlayer());
        }
        startLocation = projectile.getLocation();

        if (shotFrom.getReleaseTime() == -1) {
            releaseTime = (20 * 4) + (!gun.isThrowable() ? 1 : 0) * (400);
        } else {
            releaseTime = shotFrom.getReleaseTime();
        }
    }

    public void tick() {
        if (!dead) {
            ticks++;
            if (projectile != null) {
                lastLocation = projectile.getLocation();

                if (stuckTo != null) {
                    try {
                        lastLocation = stuckTo.getLocation().add(0, 0.5, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (bulletType.equals("crossbow")) {
                    if (released) {
                        if (ticks > releaseTime) {
                            try {
                                explode();
                                remove();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    } else {
                        if (ticks % 20 == 0) {
                            // Util.playSound(Sound.NOTE_PLING, lastLocation, 5, 4);
                        }
                    }
                }

                if (ticks > releaseTime) {
                    // 12-16 - REMOVING THIS. ADDING BETTER MOLLY EFFECT THIS IS ONLY POLACE WHERE THIS CODE IS USED LUL + IT BLOWS
                    EffectType eff = shotFrom.getReleaseEffect();
                    if (eff != null) {
                        if (eff.getType() == Effect.MOBSPAWNER_FLAMES && GunTests.WAR_MOLLY.isActive())
                            doFireExplode();
                        else
                            eff.start(lastLocation);
                    }
                    dead = true;
                    return;
                }

                if (shotFrom.isHasSmokeTrail()) {
                    lastLocation.getWorld().playEffect(lastLocation, Effect.SMOKE, 0);
                }

                if (shotFrom.isThrowable() && ticks == (int) (20 * 4.5)) {
                    remove();
                    return;
                }

                if (active) {
                    if (lastLocation.getWorld().equals(startLocation.getWorld())) {
                        double dis = lastLocation.distance(startLocation);
                        if (dis > shotFrom.getMaxDistance()) {
                            active = false;
                            if (!shotFrom.isThrowable() && !shotFrom.isCanGoPastMaxDistance())
                                velocity.multiply(0.25);
                        }
                    }
                    projectile.setVelocity(velocity);
                }
            } else {
                dead = true;
            }
            if (ticks > (20 * 10)) {
                dead = true;
            }
        } else {
            remove();
        }

        if (destroyNextTick)
            dead = true;
    }

    public void remove() {
        dead = true;
        OldGuns.getInstance().removeBullet(this);
        if (projectile != null)
            projectile.remove();
        onHit();
        destroy();
    }

    public void onHit() {
        if (released)
            return;
        released = true;
        if (projectile != null) {
            lastLocation = projectile.getLocation();

            if (destroyWhenHit) {
                if (shotFrom != null) {
                    explode();
                    fireSpread();
                    flash();
                }
            }
        }
    }

    private void doFireExplode() {
        double radius = shotFrom.getFireRadius();
        List<Block> fireBlocks = new LinkedList<>();

        // initialize custom explosion
        CustomExplosion explosion = CustomExplosion.at(lastLocation.clone());
        explosion.setSize(radius);
        explosion.setBreakBlocks(false);
        explosion.setDamageEntities(false);
        explosion.setChainExplosions(false);
        explosion.setDropYield(0);
        explosion.setFireChance(0.6);
        explosion.igniteListener(fireBlocks::add);

        // call event
        Explosion.callExplosionEvent(lastLocation.clone());

        // boom
        explosion.boom();

        // remove after duration expires
        Sync.get().delay(FIRE_TIME).run(() -> {
            for (Block b : fireBlocks) {
                if (b.getType() == Material.FIRE) {
                    b.setType(Material.AIR);
                }
            }
        });
    }

    @SuppressWarnings("null")
    public void explode() {

        if (shotFrom.getFireRadius() > 0) {
            if (GunTests.WAR_MOLLY.isActive())
                doFireExplode();
            else {
                int rad = (int) shotFrom.getFireRadius();
                int rad2 = 2;
                for (int i = -rad; i <= rad; i++) {
                    for (int ii = -rad2 / 2; ii <= rad2 / 2; ii++) {
                        for (int iii = -rad; iii <= rad; iii++) {
                            Location nloc = lastLocation.clone().add(i, ii, iii);
                            if (nloc.distance(lastLocation) <= rad && OldGuns.getInstance().getRandom().nextInt(5) == 1) {
                                lastLocation.getWorld().playEffect(nloc, Effect.MOBSPAWNER_FLAMES, 2);
                                // lastLocation.getWorld().playEffect(nloc, Effect.getById(2001),
                                // Material.FIRE);
                            }
                        }
                    }
                }
            }
        }

        if (shotFrom.getExplodeRadius() > 0) {
            lastLocation.getWorld().createExplosion(lastLocation, 0);

            int rad = (int) shotFrom.getExplodeRadius();
            if (rad > 0) {
                for (int i = -rad; i <= rad; i++) {
                    for (int ii = -rad / 2; ii <= rad / 2; ii++) {
                        for (int iii = -rad; iii <= rad; iii++) {
                            Location nloc = lastLocation.clone().add(i, ii, iii);
                            if (nloc.distance(lastLocation) <= rad && OldGuns.getInstance().getRandom().nextInt(10) == 1)
                                new Explosion(nloc).explode();
                            else {
                                Explosion.callExplosionEvent(nloc);
                            }
                        }
                    }
                }
                new Explosion(lastLocation).explode();
            }

            Location temp = lastLocation.clone().add(0, 0.55, 0);
            double c = (shotFrom.getExplodeRadius());
            ArrayList<Entity> entities = RaycastHelper.getNearbyEntities(temp, c);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity) {
                    if (RaycastHelper.hasLineOfSight(temp, ((LivingEntity) entity).getEyeLocation())) {
                        // if (((LivingEntity)entities.get(i)).hasLineOfSight(projectile)) {
                        int dmg = shotFrom.getExplosionDamage();
                        if (dmg == -1)
                            dmg = shotFrom.getGunDamage();

                        if (entity instanceof Player) {
                            LivingEntity hurt = (LivingEntity) entity;
                            ProjectileDamageEvent event = new ProjectileDamageEvent(shotFrom, shooter, dmg, ProjectileDamageEvent.ProjectileType.GRENADE, hurt);
                            event.callEvent();

                            if (!event.isCancelled()) {
                                hurt.damage(dmg, shooter.getPlayer());
                                hurt.setLastDamage(0D);
                            }
                        } else {
                            LivingEntity hurt = (LivingEntity) entity;
                            ProjectileDamageEvent event = new ProjectileDamageEvent(shotFrom, shooter, dmg, ProjectileDamageEvent.ProjectileType.GRENADE, hurt);
                            event.callEvent();

                            if (!event.isCancelled()) {
                                hurt.damage(dmg, shooter.getPlayer());
                                hurt.setLastDamage(0D);
                            }
                        }
                    }
                }
            }
        }
    }

    public void fireSpread() {
        if (shotFrom.getFireRadius() > 0) {
            lastLocation.getWorld().playSound(lastLocation, Sound.GLASS, 20, 20);
            int c = (int) (shotFrom.getFireRadius());
            ArrayList<Entity> entities = (ArrayList<Entity>) projectile.getNearbyEntities(c, c, c);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity) {
                    LivingEntity hurt = (LivingEntity) entity;
                    ProjectileDamageEvent event = new ProjectileDamageEvent(shotFrom, shooter, 1D, ProjectileDamageEvent.ProjectileType.MOLOTOV, hurt);
                    event.callEvent();

                    if (!event.isCancelled()) {
                        hurt.setFireTicks(20 * 3);
                        hurt.damage(1D, shooter.getPlayer());
                        OldGuns.resetPlayerDamage(hurt, 0);
                        //						hurt.setLastDamage(0D);
                    }
                }
            }
        }
    }

    public void flash() {
        if (shotFrom.getFlashRadius() > 0) {
            lastLocation.getWorld().playSound(lastLocation, Sound.SPLASH, 1F, 3F);

            Location temp = lastLocation.clone().add(0, 0.55, 0);
            int c = (int) (shotFrom.getFlashRadius());
            ArrayList<Entity> entities = RaycastHelper.getNearbyEntities(temp, c);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity) {
                    LivingEntity lent = (LivingEntity) entity;
                    if (RaycastHelper.hasLineOfSight(temp, lent.getEyeLocation())) {
                        ProjectileDamageEvent event = new ProjectileDamageEvent(shotFrom, shooter, 0D, ProjectileDamageEvent.ProjectileType.FLASHBANG, lent);
                        event.callEvent();

                        if (!event.isCancelled()) {
                            if (GunTests.FLASH_TURNING.isActive()) {
                                Vec3 direction = Vec3.v(lent.getEyeLocation().getDirection());
                                Vec3 pos = Vec3.v(temp);

                                double scaledAngle = (180.0D - Math.toDegrees(pos.angle(direction))) / 180.0D;
                                int scaledTicks = (int) (MAX_FLASH * scaledAngle);
                                lent.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, scaledTicks, 1));
                            } else {
                                ((LivingEntity) entity)
                                        .addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 1));
                            }
                        }
                    }
                }
            }
        }
    }

    public void destroy() {
        projectile = null;
        velocity = null;
        shotFrom = null;
        shooter = null;
    }
}
