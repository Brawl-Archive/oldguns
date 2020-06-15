package com.orange451.pvpgunplus.gun;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.orange451.pvpgunplus.PVPGunPlus;
import com.orange451.pvpgunplus.events.PVPGunPlusFireGunEvent;
import com.orange451.pvpgunplus.events.PVPGunPlusReloadGunEvent;

public class Gun {
	private boolean canHeadshot;//

	private boolean isThrowable;//

	private boolean hasSmokeTrail;

	private boolean localGunSound;

	private boolean canAimLeft;//
	private boolean canAimRight;//

	private boolean canGoPastMaxDistance;//
	private boolean destroyBulletWhenHit = true;//

	private byte gunByte;
	private byte ammoByte;

	private int gunType;
	private int ammoType;

	private int ammoAmtNeeded;

	private int gunDamage;

	private int explosionDamage = -1;

	private int roundsPerBurst;

	private int reloadTime;
	private int maxDistance;
	private int bulletsPerClick;
	private int bulletsShot;
	private int bulletDelay = 2;
	private int armorPenetration;
	private int releaseTime = -1;

	private double bulletSpeed;

	private double accuracy;
	private double accuracy_aimed = -1;
	private double accuracy_crouched = -1;

	private double explodeRadius;
	private double fireRadius;
	private double flashRadius;
	private double knockback;
	private double recoil;
	private double gunVolume = 1;
	private String gunName;
	private String fileName;

	public String projType = "";
	public ArrayList<String> gunSound = new ArrayList<String>();
	public String outOfAmmoMessage = "";
	public String permissionMessage = "";
	public boolean needsPermission;
	public boolean canClickRight;
	public boolean canClickLeft;
	public boolean hasClip = true;
	public boolean ignoreItemData = false;
	public boolean reloadGunOnDrop = true;
	public int maxClipSize = 30;
	public int bulletDelayTime = 10;

	public int roundsFired;
	public int gunReloadTimer;
	public int timer;
	public int lastFired;
	public int ticks;
	public int heldDownTicks;
	public boolean firing = false;
	public boolean reloading;
	public boolean changed = false;
	public EffectType releaseEffect;

	public GunPlayer owner;
	public String node;
	public String reloadType = "NORMAL";

	public Gun(String name) {
		this.gunName = name;
		this.fileName = name;
		this.outOfAmmoMessage = "Out of ammo!";
	}

	public void shoot() {
		if (owner != null) {
			if (owner.canShoot(this)) {
				if (owner.getPlayer().isOnline()) {
					if (!reloading) {
						PVPGunPlusFireGunEvent event = new PVPGunPlusFireGunEvent(owner, this);
						event.callEvent();
						if (!event.isCancelled()) {
							if ((owner.checkAmmo(this, event.getAmountAmmoNeeded()) && event.getAmountAmmoNeeded() > 0)
									|| event.getAmountAmmoNeeded() == 0) {
								owner.removeAmmo(this, event.getAmountAmmoNeeded());
								execute_shoot(event.getGunAccuracy());
								owner.setLastShot(this);
							} else {
								owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.ITEM_BREAK, 20, 20);
								owner.getPlayer().sendMessage(this.outOfAmmoMessage);
								finishShooting();
							}
						}
					}

				}
			}
		}
	}

	protected void execute_shoot(double ev_acc) {
		if (owner != null) {
			if (owner.getPlayer().isOnline()) {
				if (roundsFired >= maxClipSize && hasClip) {
					reloadGun();
					return;
				}
				doRecoil(owner.getPlayer());
				changed = true;
				roundsFired++;
				for (int i = 0; i < gunSound.size(); i++) {
					Sound sound = PVPGunPlus.getSound(gunSound.get(i));
					if (sound != null) {
						if (this.localGunSound)
							owner.getPlayer().playSound(owner.getPlayer().getLocation(), sound, (float) gunVolume, 2);
						else
							owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), sound,
									(float) gunVolume, 2);
					}
				}

				for (int i = 0; i < this.bulletsPerClick; i++) {
					int acc = (int) (ev_acc * 1000);

					if (acc <= 0)
						acc = 1;

					Location ploc = owner.getPlayer().getLocation();
					Random rand = new Random();
					double dir = -ploc.getYaw() - 90.0F;
					double pitch = -ploc.getPitch();
					double xwep = (rand.nextInt(acc) - rand.nextInt(acc) + 0.5D) / 1000.0D;
					double ywep = (rand.nextInt(acc) - rand.nextInt(acc) + 0.5D) / 1000.0D;
					double zwep = (rand.nextInt(acc) - rand.nextInt(acc) + 0.5D) / 1000.0D;
					double xd = Math.cos(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + xwep;
					double yd = Math.sin(Math.toRadians(pitch)) + ywep;
					double zd = -Math.sin(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + zwep;
					Vector vec = new Vector(xd, yd, zd);
					vec.multiply(getBulletSpeed());
					Bullet bullet = new Bullet(owner, vec, this, null);
					PVPGunPlus.getPlugin().addBullet(bullet);
				}

				if (roundsFired >= maxClipSize && hasClip) {
					reloadGun();
					return;
				}
			}
		}
	}

	public void tick() {
		ticks++;
		lastFired++;
		timer--;
		gunReloadTimer--;

		if (gunReloadTimer < 0) {
			if (reloading) {
				finishReloading();
			}
			reloading = false;
		}

		gunSounds();

		if (lastFired > 6)
			heldDownTicks = 0;

		if (((heldDownTicks >= 2 && timer <= 0) || firing) && !reloading) {
			if (this.roundsPerBurst > 1) {
				if (ticks % this.bulletDelay == 0) {
					bulletsShot++;
					if (bulletsShot <= this.roundsPerBurst) {
						shoot();
					} else {
						finishShooting();
					}
				}
			} else {
				shoot();
				finishShooting();
			}
		}

		if (reloading) {
			firing = false;
		}
	}

	public Gun copy() {
		Gun g = new Gun(gunName);
		g.gunName = gunName;
		g.gunType = gunType;
		g.gunByte = gunByte;
		g.ammoByte = ammoByte;
		g.ammoAmtNeeded = ammoAmtNeeded;
		g.ammoType = ammoType;
		g.roundsPerBurst = roundsPerBurst;
		g.bulletsPerClick = bulletsPerClick;
		g.setBulletSpeed(bulletSpeed);
		g.accuracy = accuracy;
		g.accuracy_aimed = accuracy_aimed;
		g.accuracy_crouched = accuracy_crouched;
		g.maxDistance = maxDistance;
		g.gunVolume = gunVolume;
		g.gunDamage = gunDamage;
		g.explodeRadius = explodeRadius;
		g.fireRadius = fireRadius;
		g.flashRadius = flashRadius;
		g.canHeadshot = canHeadshot;
		g.reloadTime = reloadTime;
		g.canAimLeft = canAimLeft;
		g.canAimRight = canAimRight;
		g.canClickLeft = canClickLeft;
		g.canClickRight = canClickRight;
		g.hasSmokeTrail = hasSmokeTrail;
		g.armorPenetration = armorPenetration;
		g.isThrowable = isThrowable;
		g.ignoreItemData = ignoreItemData;
		g.outOfAmmoMessage = outOfAmmoMessage;
		g.projType = projType;
		g.needsPermission = needsPermission;
		g.node = node;
		g.gunSound = gunSound;
		g.bulletDelayTime = bulletDelayTime;
		g.hasClip = hasClip;
		g.maxClipSize = maxClipSize;
		g.reloadGunOnDrop = reloadGunOnDrop;
		g.localGunSound = localGunSound;
		g.fileName = fileName;
		g.explosionDamage = explosionDamage;
		g.recoil = recoil;
		g.knockback = knockback;
		g.reloadType = reloadType;
		g.releaseTime = releaseTime;
		g.canGoPastMaxDistance = canGoPastMaxDistance;
		g.permissionMessage = permissionMessage;
		g.destroyBulletWhenHit = this.destroyBulletWhenHit;
		if (this.releaseEffect != null) {
			g.releaseEffect = this.releaseEffect.clone();
		}

		return g;
	}

	public void reloadGun() {
		PVPGunPlusReloadGunEvent pvpgunrel = new PVPGunPlusReloadGunEvent(owner, this);
		pvpgunrel.callEvent();
		if (!pvpgunrel.isCancelled()) {
			gunReloadTimer = pvpgunrel.getReloadTime();
			reloading = true;
		}
	}

	private void gunSounds() {
		if (reloading) {
			int amtReload = this.reloadTime - this.gunReloadTimer;
			if (this.reloadType.equalsIgnoreCase("bolt")) {
				if (amtReload == 6) {
					if (Bukkit.getPluginManager().getPlugin("McWarZ") != null)
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.DOOR_OPEN, 2,
								(float) 1.5);
					else
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.DOOR_OPEN, 2, (float) 1.5);
				}
				if (amtReload == reloadTime - 4) {
					if (Bukkit.getPluginManager().getPlugin("McWarZ") != null)
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.DOOR_CLOSE, 1,
								(float) 1.5);
					else
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.DOOR_CLOSE, 1, (float) 1.5);
				}
			} else if (this.reloadType.equalsIgnoreCase("pump") || (this.reloadType.equals("INDIVIDUAL_BULLET"))) {
				int rep = ((this.reloadTime - 10) / this.maxClipSize);
				if (amtReload >= 5 && amtReload <= this.reloadTime - 5 && (amtReload % (rep) == 0)) {
					if (Bukkit.getPluginManager().getPlugin("McWarZ") != null) {
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.NOTE_STICKS, 1,
								1);
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.NOTE_SNARE_DRUM,
								1, 2);
					} else {
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.NOTE_STICKS, 1, 1);
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.NOTE_SNARE_DRUM, 1, 2);
					}
				}

				if (amtReload == reloadTime - 3) {
					if (Bukkit.getPluginManager().getPlugin("McWarZ") != null)
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.PISTON_EXTEND, 1,
								2);
					else
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.PISTON_EXTEND, 1, 2);
				}
				if (amtReload == reloadTime - 1) {
					if (Bukkit.getPluginManager().getPlugin("McWarZ") != null)
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.PISTON_RETRACT, 1,
								2);
					else
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.PISTON_RETRACT, 1, 2);
				}
			} else {
				if (amtReload == 6) {
					if (Bukkit.getPluginManager().getPlugin("McWarZ") != null) {
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.FIRE_IGNITE, 2,
								(float) 2);
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.DOOR_OPEN, 1,
								(float) 2);
					} else {
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.FIRE_IGNITE, 2, (float) 2);
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.DOOR_OPEN, 1, (float) 2);
					}
				}
				if (amtReload == reloadTime / 2) {
					if (Bukkit.getPluginManager().getPlugin("McWarZ") != null)
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.PISTON_RETRACT,
								0.33f, (float) 2);
					else
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.PISTON_RETRACT, 0.33f,
								(float) 2);
				}
				if (amtReload == reloadTime - 4) {
					if (Bukkit.getPluginManager().getPlugin("McWarZ") != null) {
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.FIRE_IGNITE, 2,
								(float) 2);
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.DOOR_CLOSE, 1f,
								(float) 2);
					} else {
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.FIRE_IGNITE, 2, (float) 2);
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.DOOR_OPEN, 1, (float) 2);
					}
				}
			}
		} else {
			if (this.reloadType.equalsIgnoreCase("pump")) {
				if (timer == 8) {
					if (Bukkit.getPluginManager().getPlugin("McWarZ") != null)
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.PISTON_EXTEND, 1,
								2);
					else
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.PISTON_EXTEND, 1, 2);
				}
				if (timer == 6) {
					if (Bukkit.getPluginManager().getPlugin("McWarZ") != null)
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.PISTON_RETRACT, 1,
								2);
					else
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.PISTON_RETRACT, 1, 2);
				}
			}

			if (this.reloadType.equalsIgnoreCase("bolt")) {
				if (timer == this.bulletDelayTime - 4) {
					if (Bukkit.getPluginManager().getPlugin("McWarZ") != null)
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.DOOR_OPEN, 2,
								(float) 1.25);
					else
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.DOOR_OPEN, 2, (float) 1.25);
				}
				if (timer == 6) {
					if (Bukkit.getPluginManager().getPlugin("McWarZ") != null)
						owner.getPlayer().getWorld().playSound(owner.getPlayer().getLocation(), Sound.DOOR_CLOSE, 1,
								(float) 1.25);
					else
						owner.getPlayer().playSound(owner.getPlayer().getLocation(), Sound.DOOR_CLOSE, 1, (float) 1.25);
				}
			}
		}
	}

	private void doRecoil(Player player) {
		if (recoil != 0) {
			Location ploc = player.getLocation();
			double dir = -ploc.getYaw() - 90.0F;
			double pitch = -ploc.getPitch() - 180;
			double xd = Math.cos(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch));
			double yd = Math.sin(Math.toRadians(pitch));
			double zd = -Math.sin(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch));
			Vector vec = new Vector(xd, yd, zd);
			vec.multiply(this.recoil / 2d).setY(0);
			player.setVelocity(player.getVelocity().add(vec));
		}
	}

	public void doKnockback(LivingEntity entity, Vector speed) {
		if (this.knockback > 0) {
            speed.normalize().setY(0.6).multiply(this.knockback / 4d);
            entity.setVelocity(speed);
//			Vector toApply = speed.normalize()/* .setY(0.6) */.multiply(this.knockback / 4d);
//			toApply = entity.getVelocity().clone().add(toApply).setY(entity.getVelocity().getY());
//			entity.setVelocity(toApply);
		}
	}

	public void finishReloading() {
		bulletsShot = 0;
		roundsFired = 0;
		changed = false;
		gunReloadTimer = 0;
	}

	private void finishShooting() {
		bulletsShot = 0;
		timer = bulletDelayTime;// reloadTime;
		firing = false;
	}

	public String getName() {
		return gunName;
	}

	public Material getAmmoMaterial() {
		int id = getAmmoType();
		Material mat = Material.getMaterial(id);
		if (mat != null) {
			return mat;
		}
		return null;
	}

	public int getAmmoType() {
		return ammoType;
	}

	public int getAmmoAmtNeeded() {
		return ammoAmtNeeded;
	}

	public Material getGunMaterial() {
		int id = getGunType();
		Material mat = Material.getMaterial(id);
		if (mat != null) {
			return mat;
		}
		System.out.println("NULL MATERIAL IN GUN: " + this.gunName + " / TYPEID: " + id);
		return null;
	}

	public int getGunType() {
		return gunType;
	}

	public double getExplodeRadius() {
		return explodeRadius;
	}

	public double getFireRadius() {
		return fireRadius;
	}

	public boolean isThrowable() {
		return isThrowable;
	}

	public void setName(String val) {
		this.gunName = ChatColor.translateAlternateColorCodes('&', val);
	}

	public int getValueFromString(String str) {
		if (str.contains(":")) {
			String news = str.substring(0, str.indexOf(":"));
			return Integer.parseInt(news);
		} else {
			return Integer.parseInt(str);
		}
	}

	public byte getByteDataFromString(String str) {
		if (str.contains(":")) {
			String news = str.substring(str.indexOf(":") + 1, str.length());
			return Byte.parseByte(news);
		}
		return -1;
	}

	public void setGunType(String val) {
		this.gunType = getValueFromString(val);
		this.gunByte = getByteDataFromString(val);
		if (gunByte == -1) {
			this.ignoreItemData = true;
			this.gunByte = 0;
		}
	}

	public void setAmmoType(String val) {
		this.ammoType = getValueFromString(val);
		this.ammoByte = getByteDataFromString(val);
		if (this.ammoByte == -1)
			this.ammoByte = 0;
	}

	public void setAmmoAmountNeeded(int parseInt) {
		this.ammoAmtNeeded = parseInt;
	}

	public void setRoundsPerBurst(int parseInt) {
		this.roundsPerBurst = parseInt;
	}

	public void setBulletsPerClick(int parseInt) {
		this.bulletsPerClick = parseInt;
	}

	public int getBulletsPerClick() {
		return bulletsPerClick;
	}

	public void setBulletSpeed(double parseDouble) {
		this.bulletSpeed = parseDouble;
	}

	public void setAccuracy(double parseDouble) {
		this.accuracy = parseDouble;
	}

	public void setAccuracyAimed(double parseDouble) {
		this.accuracy_aimed = parseDouble;
	}

	public void setAccuracyCrouched(double parseDouble) {
		this.accuracy_crouched = parseDouble;
	}

	public void setExplodeRadius(double parseDouble) {
		this.explodeRadius = parseDouble;
	}

	public void setFireRadius(double parseDouble) {
		this.fireRadius = parseDouble;
	}

	public void setCanHeadshot(boolean parseBoolean) {
		this.canHeadshot = parseBoolean;
	}

	public void setCanClickLeft(boolean parseBoolean) {
		this.canClickLeft = parseBoolean;
	}

	public void setCanClickRight(boolean parseBoolean) {
		this.canClickRight = parseBoolean;
	}

	public void clear() {
		this.owner = null;
	}

	public void setReloadTime(int parseInt) {
		this.reloadTime = parseInt;
	}

	public int getReloadTime() {
		return reloadTime;
	}

	public int getGunDamage() {
		return this.gunDamage;
	}

	public void setGunDamage(int parseInt) {
		this.gunDamage = parseInt;
	}

	public double getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(int i) {
		this.maxDistance = i;
	}

	public boolean canAimLeft() {
		return canAimLeft;
	}

	public boolean canAimRight() {
		return canAimRight;
	}

	public void setCanAimLeft(boolean parseBoolean) {
		this.canAimLeft = parseBoolean;
	}

	public void setCanAimRight(boolean parseBoolean) {
		this.canAimRight = parseBoolean;
	}

	public void setOutOfAmmoMessage(String val) {
		this.outOfAmmoMessage = ChatColor.translateAlternateColorCodes('&', val);
	}

	public void setPermissionMessage(String val) {
		this.permissionMessage = ChatColor.translateAlternateColorCodes('&', val);
	}

	public void setFlashRadius(double parseDouble) {
		flashRadius = parseDouble;
	}

	public double getFlashRadius() {
		return flashRadius;
	}

	public void setIsThrowable(boolean b) {
		this.isThrowable = b;
	}

	public boolean canHeadShot() {
		return this.canHeadshot;
	}

	public boolean hasSmokeTrail() {
		return hasSmokeTrail;
	}

	public void setSmokeTrail(boolean b) {
		hasSmokeTrail = b;
	}

	public boolean isLocalGunSound() {
		return localGunSound;
	}

	public void setLocalGunSound(boolean b) {
		this.localGunSound = b;
	}

	public void setArmorPenetration(int parseInt) {
		this.armorPenetration = parseInt;
	}

	public int getArmorPenetration() {
		return armorPenetration;
	}

	public void setExplosionDamage(int i) {
		this.explosionDamage = i;
	}

	public int getExplosionDamage() {
		return this.explosionDamage;
	}

	public String getFilename() {
		return fileName;
	}

	public void setFilename(String string) {
		this.fileName = string;
	}

	public void setGunTypeByte(byte b) {
		this.gunByte = b;
	}

	public byte getGunTypeByte() {
		return gunByte;
	}

	public void setAmmoTypeByte(byte b) {
		this.ammoByte = b;
	}

	public byte getAmmoTypeByte() {
		return this.ammoByte;
	}

	public void setRecoil(double d) {
		this.recoil = d;
	}

	public double getRecoil() {
		return recoil;
	}

	public void setKnockback(double d) {
		this.knockback = d;
	}

	public double getKnockback() {
		return this.knockback;
	}

	public void addGunSounds(String val) {
		String[] sounds = val.split(",");
		for (int i = 0; i < sounds.length; i++) {
			gunSound.add(sounds[i]);
		}
	}

	public int getReleaseTime() {
		return releaseTime;
	}

	public void setReleaseTime(int v) {
		this.releaseTime = v;
	}

	public void setCanGoPastMaxDistance(boolean parseBoolean) {
		this.canGoPastMaxDistance = parseBoolean;
	}

	public boolean canGoPastMaxDistance() {
		return canGoPastMaxDistance;
	}

	public void setGunVolume(double parseDouble) {
		this.gunVolume = parseDouble;
	}

	public double getGunVolume() {
		return gunVolume;
	}

	public double getAccuracy() {
		return this.accuracy;
	}

	public double getAccuracy_aimed() {
		return this.accuracy_aimed;
	}

	public double getAccuracy_crouched() {
		return this.accuracy_crouched;
	}

	public double getBulletSpeed() {
		return bulletSpeed;
	}

	public void setDestroyBulletWhenHit(boolean b) {
		this.destroyBulletWhenHit = b;
	}

	public boolean getDestroyBulletWhenHit() {
		return destroyBulletWhenHit;
	}

	public void setBulletDelayTime(int i) {
		this.bulletDelayTime = i;
	}

	public int getBulletDelayTime() {
		return bulletDelayTime;
	}
}
