package com.orange451.pvpgunplus.gun;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Item;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.orange451.pvpgunplus.PVPGunExplosion;
import com.orange451.pvpgunplus.PVPGunPlus;
import com.orange451.pvpgunplus.RaycastHelper;
import com.orange451.pvpgunplus.events.PVPGunPlusGunKillEntityEvent;
import com.orange451.pvpgunplus.events.PVPGunPlusProjectileDamageEvent;
import com.orange451.pvpgunplus.events.PVPGunPlusProjectileDamageEvent.ProjectileType;

import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.Explosion;

public class Bullet {
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
	public String bulletType = "";

	public boolean destroyWhenHit;

	public Bullet(GunPlayer owner, Vector vec, Gun gun, Projectile alreadyFired) {
		shotFrom = gun;
		shooter = owner;
		velocity = vec;

		destroyWhenHit = gun.getDestroyBulletWhenHit();

		if (alreadyFired != null) {
			projectile = alreadyFired;
		}

		if (gun.isThrowable()) {
			ItemStack thrown = new ItemStack(gun.getGunType(), 1, gun.getGunTypeByte());
			projectile = owner.getPlayer().getWorld().dropItem(owner.getPlayer().getEyeLocation(), thrown);
			((Item) projectile).setPickupDelay(9999999);
			startLocation = projectile.getLocation();
		} else {
			Class<? extends Projectile> mclass = Snowball.class;
			String check = gun.projType.replace(" ", "").replace("_", "");
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
				final GunPlayer mshooter = shooter;
				final Gun mgun = shotFrom;
				Location to = b_hit.getLocation();

				if ((p_hit != null) && (loc.distance(p_hit.getLocation()) < loc.distance(b_hit.getLocation()))) {
					to = p_hit.getLocation();

					try {
						Plugin plugin = Bukkit.getPluginManager().getPlugin("KitPvP");
						if (plugin != null && plugin instanceof DamageListener) {
							DamageListener mcwarfare = (DamageListener) plugin;
							mcwarfare.damagePlayer(p_hit, shotFrom.getGunDamage(), DamageType.PLAYER,
									shooter.getPlayer());
							if ((p_hit.isDead() || ((Damageable) p_hit).getHealth() <= 0)) {
								PVPGunPlus.getPlugin().getServer().getScheduler()
										.scheduleSyncDelayedTask(PVPGunPlus.getPlugin(), new Runnable() {
											public void run() {
												PVPGunPlusGunKillEntityEvent pvpgunkill = new PVPGunPlusGunKillEntityEvent(
														mshooter, mgun, p_hit);
												pvpgunkill.callEvent();
											}
										}, 1l);
							}
						} else {
							p_hit.damage(shotFrom.getGunDamage(), shooter.getPlayer());
						}
					} catch (Exception e) {
						//
					}
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
			startLocation = projectile.getLocation();
		}

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
					EffectType eff = shotFrom.releaseEffect;
					if (eff != null) {
						eff.start(lastLocation);
					}
					dead = true;
					return;
				}

				if (shotFrom.hasSmokeTrail()) {
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
							if (!shotFrom.isThrowable() && !shotFrom.canGoPastMaxDistance())
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

	public Gun getGun() {
		return shotFrom;
	}

	public GunPlayer getShooter() {
		return shooter;
	}

	public Vector getVelocity() {
		return velocity;
	}

	public void remove() {
		dead = true;
		PVPGunPlus.getPlugin().removeBullet(this);
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

	@SuppressWarnings("null")
	public void explode() {

		if (shotFrom.getFireRadius() > 0) {
			int rad = (int) shotFrom.getFireRadius();
			int rad2 = 2;
			for (int i = -rad; i <= rad; i++) {
				for (int ii = -rad2 / 2; ii <= rad2 / 2; ii++) {
					for (int iii = -rad; iii <= rad; iii++) {
						Location nloc = lastLocation.clone().add(i, ii, iii);
						if (nloc.distance(lastLocation) <= rad && PVPGunPlus.getPlugin().random.nextInt(5) == 1) {
							lastLocation.getWorld().playEffect(nloc, Effect.MOBSPAWNER_FLAMES, 2);
							// lastLocation.getWorld().playEffect(nloc, Effect.getById(2001),
							// Material.FIRE);
						}
					}
				}
			}
		}

		if (shotFrom.getExplodeRadius() > 0) {
			lastLocation.getWorld().createExplosion(lastLocation, 0);

			int rad = (int) shotFrom.getExplodeRadius();
			int rad2 = rad;
			if (rad > 0) {
				for (int i = -rad; i <= rad; i++) {
					for (int ii = -rad2 / 2; ii <= rad2 / 2; ii++) {
						for (int iii = -rad; iii <= rad; iii++) {
							Location nloc = lastLocation.clone().add(i, ii, iii);
							if (nloc.distance(lastLocation) <= rad && PVPGunPlus.getPlugin().random.nextInt(10) == 1)
								new PVPGunExplosion(nloc).explode();
							else {
								PVPGunExplosion.callExplosionEvent(nloc);
							}
						}
					}
				}
				new PVPGunExplosion(lastLocation).explode();
			}

			Location temp = lastLocation.clone().add(0, 0.55, 0);
			double c = (shotFrom.getExplodeRadius());
			ArrayList<Entity> entities = RaycastHelper.getNearbyEntities(temp, c);
			for (int i = 0; i < entities.size(); i++) {
				if (entities.get(i) instanceof LivingEntity) {
					if (RaycastHelper.hasLineOfSight(temp, ((LivingEntity) entities.get(i)).getEyeLocation())) {
						// if (((LivingEntity)entities.get(i)).hasLineOfSight(projectile)) {
						int dmg = shotFrom.getExplosionDamage();
						if (dmg == -1)
							dmg = shotFrom.getGunDamage();

						if (entities.get(i) instanceof Player) {
							Plugin plugin = Bukkit.getPluginManager().getPlugin("KitPvP");
							if (plugin != null && plugin instanceof DamageListener) {
								DamageListener mcwarfare = (DamageListener) plugin;
								mcwarfare.damagePlayer(((Player) entities.get(i)), dmg, DamageType.EXPLOSION,
										shooter.getPlayer());
							} else {
								LivingEntity hurt = (LivingEntity) entities.get(i);
								PVPGunPlusProjectileDamageEvent event = new PVPGunPlusProjectileDamageEvent(shotFrom, shooter, dmg, ProjectileType.GRENADE, hurt);
								event.callEvent();
								
								if(!event.isCancelled()) {
									hurt.damage(dmg, shooter.getPlayer());
									hurt.setLastDamage(0D);
								}
							}
						} else {
							LivingEntity hurt = (LivingEntity) entities.get(i);
							PVPGunPlusProjectileDamageEvent event = new PVPGunPlusProjectileDamageEvent(shotFrom, shooter, dmg, ProjectileType.GRENADE, hurt);
							event.callEvent();
							
							if(!event.isCancelled()) {
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
			for (int i = 0; i < entities.size(); i++) {
				if (entities.get(i) instanceof LivingEntity) {
					LivingEntity hurt = (LivingEntity) entities.get(i);
					PVPGunPlusProjectileDamageEvent event = new PVPGunPlusProjectileDamageEvent(shotFrom, shooter, 1D, ProjectileType.MOLOTOV, hurt);
					event.callEvent();
					
					if(!event.isCancelled()) {
						hurt.damage(1D, shooter.getPlayer());
						hurt.setLastDamage(0D);
						hurt.setFireTicks(20 * 3);
					}
				}
			}
		}
	}

	public void flash() {
		if (shotFrom.getFlashRadius() > 0) {
			lastLocation.getWorld().playSound(lastLocation, Sound.SPLASH, 20, 20);

			Location temp = lastLocation.clone().add(0, 0.55, 0);
			int c = (int) (shotFrom.getFlashRadius());
			ArrayList<Entity> entities = RaycastHelper.getNearbyEntities(temp, c);
			for (int i = 0; i < entities.size(); i++) {
				if (entities.get(i) instanceof LivingEntity) {
					if (RaycastHelper.hasLineOfSight(temp, ((LivingEntity) entities.get(i)).getEyeLocation())) {
						LivingEntity hurt = (LivingEntity) entities.get(i);
						PVPGunPlusProjectileDamageEvent event = new PVPGunPlusProjectileDamageEvent(shotFrom, shooter, 0D, ProjectileType.FLASHBANG, hurt);
						event.callEvent();
						
						if(!event.isCancelled()) {
							((LivingEntity) entities.get(i))
							.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 7, 1));
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

	public Entity getProjectile() {
		return projectile;
	}

	public void setNextTickDestroy() {
		destroyNextTick = true;
	}

	public void setStuckTo(LivingEntity hurt) {
		stuckTo = hurt;
	}
}
