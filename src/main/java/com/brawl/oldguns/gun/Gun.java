package com.brawl.oldguns.gun;

import com.brawl.oldguns.OldGuns;
import com.brawl.oldguns.events.FireGunEvent;
import com.brawl.oldguns.events.ReloadGunEvent;
import com.brawl.oldguns.util.InventoryHelper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

@Getter
@Setter
public class Gun {
    private String projType = "";
    private ArrayList<String> gunSound = new ArrayList<>();
    private String outOfAmmoMessage;
    private String permissionMessage = "";
    private boolean needsPermission;
    private boolean canClickRight;
    private boolean canClickLeft;
    private boolean hasClip = true;
    private boolean ignoreItemData = false;
    private boolean reloadGunOnDrop = true;
    private int maxClipSize = 30;
    private int bulletDelayTime = 10;
    private int roundsFired;
    private int gunReloadTimer;
    private int timer;
    private int lastFired;
    private int ticks;
    private int heldDownTicks;
    private boolean firing = false;
    private boolean reloading;
    private boolean changed = false;
    private EffectType releaseEffect;
    private GunPlayer owner;
    private String node;
    private String reloadType = "NORMAL";
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
    private boolean infiniteAmmo = false;
    private int ammoAmtNeeded;
    private int gunDamage;
    private int explosionDamage = -1;
    private int roundsPerBurst;
    private int reloadTime;
    private int maxDistance;
    private int bulletsPerClick;
    private int bulletsShot;
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
                        FireGunEvent event = new FireGunEvent(owner, this);
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
                for (String s : gunSound) {
                    Sound sound = OldGuns.getSound(s);
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
                    OldGuns.getInstance().addBullet(bullet);
                }

                if (roundsFired >= maxClipSize && hasClip) {
                    reloadGun();
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
                int bulletDelay = 2;
                if (ticks % bulletDelay == 0) {
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
        g.infiniteAmmo = this.infiniteAmmo;

        return g;
    }

    public void reloadGun() {
        ReloadGunEvent pvpgunrel = new ReloadGunEvent(owner, this);
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

        if (infiniteAmmo && owner != null && owner.getPlayer() != null && owner.getPlayer().isOnline()) {
            int ammo = InventoryHelper.amtItem(owner.getPlayer().getInventory(), getAmmoType(), getAmmoByte());
            int needed = Math.max(0, (maxClipSize - ammo));

            if (needed != 0) {
                owner.getPlayer().getInventory().addItem(new ItemStack(getAmmoMaterial(), needed));
            }
        }
    }

    private void finishShooting() {
        bulletsShot = 0;
        timer = bulletDelayTime;// reloadTime;
        firing = false;

        if (infiniteAmmo && owner != null && owner.getPlayer() != null && owner.getPlayer().isOnline()) {
            int ammo = InventoryHelper.amtItem(owner.getPlayer().getInventory(), getAmmoType(), getAmmoByte());
            int needed = Math.max(0, (maxClipSize - ammo));

            if (needed != 0) {
                owner.getPlayer().getInventory().addItem(new ItemStack(getAmmoMaterial(), needed));
            }
        }
    }

    public void setName(String val) {
        this.gunName = ChatColor.translateAlternateColorCodes('&', val);
    }

    public Material getAmmoMaterial() {
        int id = getAmmoType();
        return Material.getMaterial(id);
    }

    public void setAmmoType(String val) {
        this.ammoType = getValueFromString(val);
        this.ammoByte = getByteDataFromString(val);
        if (this.ammoByte == -1)
            this.ammoByte = 0;
    }

    public void clear() {
        this.owner = null;
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

    public void setGunType(String val) {
        this.gunType = getValueFromString(val);
        this.gunByte = getByteDataFromString(val);
        if (gunByte == -1) {
            this.ignoreItemData = true;
            this.gunByte = 0;
        }
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
            String news = str.substring(str.indexOf(":") + 1);
            return Byte.parseByte(news);
        }
        return -1;
    }

    public void setOutOfAmmoMessage(String val) {
        this.outOfAmmoMessage = ChatColor.translateAlternateColorCodes('&', val);
    }

    public void setPermissionMessage(String val) {
        this.permissionMessage = ChatColor.translateAlternateColorCodes('&', val);
    }

    public void addGunSounds(String val) {
        String[] sounds = val.split(",");
        gunSound.addAll(Arrays.asList(sounds));
    }
}
