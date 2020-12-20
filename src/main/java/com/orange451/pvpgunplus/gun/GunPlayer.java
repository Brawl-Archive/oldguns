package com.orange451.pvpgunplus.gun;

import com.brawl.base.util.scheduler.*;
import com.orange451.pvpgunplus.*;
import lombok.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.*;

import java.util.*;

public class GunPlayer {

    private static Integer GUN_SWAP_COOLDOWN = null;
    private final ArrayList<Gun> guns;
    private final int gunSwapCooldown;
    public long lastHit;
    public Location lastHitLocation;
    public GunPlayer lastHitSource;
    public boolean takeLavaDamage = true;
    private int ticks;
    private Player controller;
    private ItemStack lastHeldItem;
    private Gun currentlyFiring;
    private Gun lastFiredGun;
    private int lastFiredTicks;
    @Getter
    @Setter
    private boolean enabled = true;

    public GunPlayer(PVPGunPlus plugin, Player player) {
        this.controller = player;
        this.guns = plugin.getLoadedGuns();
        for (int i = 0; i < guns.size(); i++) {
            guns.get(i).owner = this;
        }

        if (GUN_SWAP_COOLDOWN == null)
            GUN_SWAP_COOLDOWN = plugin.getConfig().getInt("gunswapcooldown", 0);

        gunSwapCooldown = GUN_SWAP_COOLDOWN;
    }

    public boolean isAimedIn() {
        if (controller == null)
            return false;
        if (!controller.isOnline())
            return false;

        return controller.hasPotionEffect(PotionEffectType.SLOW);
    }

    public boolean canShoot(Gun gun) {
        return !(lastFiredGun != null && lastFiredGun != gun && ticks - lastFiredTicks <= gunSwapCooldown);
    }

    public boolean onClick(String clickType, Projectile alreadyFired) {
        if (!enabled)
            return false;

        Gun holding = null;
        ItemStack hand = controller.getItemInHand();
        if (hand != null) {
            ArrayList<Gun> tempgun = this.getGunsByType(hand);
            ArrayList<Gun> canFire = new ArrayList<Gun>();
            for (int i = 0; i < tempgun.size(); i++) {
                if (PermissionInterface.checkPermission(controller, tempgun.get(i).node) || !tempgun.get(i).needsPermission) {
                    canFire.add(tempgun.get(i));
                }
            }
            if (tempgun.size() > canFire.size() && canFire.size() == 0) {
                if (tempgun.get(0).permissionMessage != null && tempgun.get(0).permissionMessage.length() > 0)
                    controller.sendMessage(tempgun.get(0).permissionMessage);
                return false;
            }
            tempgun.clear();
            for (int i = 0; i < canFire.size(); i++) {
                Gun check = canFire.get(i);
                byte gunDat = check.getGunTypeByte();
                byte itmDat = hand.getData().getData();

                if (gunDat == itmDat || check.ignoreItemData)
                    holding = check;
            }
            canFire.clear();
        }
        if (holding != null) {
            String shootAction = "right";
            if (holding.projType.replace(" ", "").replace("_", "").equals("crossbow"))
                shootAction = "bow";
            if ((holding.canClickRight || holding.canAimRight()) && clickType.equals(shootAction)) {
                if (!holding.canAimRight()) {
                    holding.heldDownTicks++;
                    holding.lastFired = 0;
                    if (currentlyFiring == null) {
                        fireGun(holding, alreadyFired);
                    }
                } else {
                    checkAim();
                }
            } else if ((holding.canClickLeft || holding.canAimLeft()) && clickType.equals("left")) {
                if (!holding.canAimLeft()) {
                    holding.heldDownTicks = 0;
                    if (currentlyFiring == null) {
                        fireGun(holding, alreadyFired);
                    }
                } else {
                    checkAim();
                }
            }
        }
        return true;
    }

    public void checkAim() {
        if (this.isAimedIn()) {
            controller.removePotionEffect(PotionEffectType.SLOW);
        } else {
            controller.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 60 * 10, 4));
        }
    }

    private void fireGun(Gun gun, Projectile alreadyFired) {
        if (PermissionInterface.checkPermission(controller, gun.node) || !gun.needsPermission) {
            if (gun.timer <= 0) {

                if (alreadyFired != null) {
                    Bullet b = new Bullet(this, alreadyFired.getVelocity(), gun, alreadyFired);
                    PVPGunPlus.getPlugin().addBullet(b);
                    this.removeAmmo(gun, gun.getAmmoAmtNeeded() - 1);
                } else {
                    this.currentlyFiring = gun;
                    gun.firing = true;
                }

                lastFiredTicks = ticks;
                lastFiredGun = gun;

            }
        } else {
            if (gun.permissionMessage != null && gun.permissionMessage.length() > 0)
                controller.sendMessage(gun.permissionMessage);
        }
    }

    public void tick() {
        ticks++;
        if (enabled && controller != null) {
            renameGuns(controller);
            ItemStack hand = controller.getItemInHand();
            this.lastHeldItem = hand;
            if (ticks % 10 == 0 && hand != null) {
                Gun g = PVPGunPlus.getPlugin().getGun(hand.getTypeId());
                if (g == null) {
                    controller.removePotionEffect(PotionEffectType.SLOW);
                }
            }
            for (int i = guns.size() - 1; i >= 0; i--) {
                Gun g = guns.get(i);
                if (g != null) {
                    g.tick();

                    if (controller.isDead()) {
                        g.finishReloading();
                    }

                    if (hand != null) {
                        if (g.getGunType() == hand.getTypeId()) {
                            if (this.isAimedIn() && !g.canAimLeft() && !g.canAimRight()) {
                                controller.removePotionEffect(PotionEffectType.SLOW);
                            }
                        }
                    }

                    if (currentlyFiring != null && g.timer <= 0 && currentlyFiring.equals(g))
                        this.currentlyFiring = null;
                }
            }
        }
    }

    public void forceFireGun(String gun) {
        Gun cgun = this.getGun(gun);
        if (cgun != null) {
            double spd = cgun.getBulletSpeed();
            cgun.setBulletSpeed(0);
            cgun.execute_shoot(0.001);
            cgun.setBulletSpeed(spd);
        }
    }

    public void renameGuns(Player p) {
        Inventory inv = p.getInventory();
        org.bukkit.inventory.ItemStack[] items = inv.getContents();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                String name = getGunName(items[i]);
                if (name != null && name.length() > 0) {
                    setName(items[i], name);
                }
            }
        }
    }

    public ArrayList<Gun> getGunsByType(ItemStack item) {
        ArrayList<Gun> ret = new ArrayList<Gun>();
        for (int i = 0; i < guns.size(); i++) {
            if (guns.get(i).getGunMaterial().equals(item.getType())) {
                ret.add(guns.get(i));
            }
        }

        return ret;
    }

    public String getGunName(org.bukkit.inventory.ItemStack item) {
        String ret = "";
        ArrayList<Gun> tempgun = getGunsByType(item);
        int amtGun = tempgun.size();
        if (amtGun > 0) {
            for (int i = 0; i < tempgun.size(); i++) {
                Gun current = tempgun.get(i);
                if (current.getGunMaterial() != null && current.getGunMaterial().getId() == item.getTypeId()) {
                    return getGunName(current);
                }
            }
        }
        return ret;
    }

    private String getGunName(Gun current) {
        String add = "";
        StringBuilder refresh = new StringBuilder();
        if (current.hasClip) {
            int leftInClip = 0;
            int ammoLeft = 0;
            int maxInClip = current.maxClipSize;

            int currentAmmo = (int) Math.floor(InventoryHelper.amtItem(controller.getInventory(), current.getAmmoType(), current.getAmmoTypeByte()) / ((double) current.getAmmoAmtNeeded()));
            ammoLeft = currentAmmo - maxInClip + current.roundsFired;
            if (ammoLeft < 0)
                ammoLeft = 0;
            leftInClip = currentAmmo - ammoLeft;
            add = ChatColor.YELLOW + "    «" + leftInClip + " │ " + (current.isInfiniteAmmo() ? "∞" : Integer.toString(ammoLeft)) + "»";
            if (current.reloading) {
                int reloadSize = 4;
                double reloadFrac = ((current.getReloadTime() - current.gunReloadTimer) / (double) current.getReloadTime());
                int amt = (int) Math.round(reloadFrac * reloadSize);
                for (int ii = 0; ii < amt; ii++) {
                    refresh.append("▪");
                }
                for (int ii = 0; ii < reloadSize - amt; ii++) {
                    refresh.append("▫");
                }

                add = ChatColor.RED + "    " + new StringBuffer(refresh.toString()).reverse() + "RELOADING" + refresh;
            }
        }
        String name = current.getName();
        return name + add;
    }

    public org.bukkit.inventory.ItemStack setName(org.bukkit.inventory.ItemStack item, String name) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        item.setItemMeta(im);

        return item;
    }

    public Player getPlayer() {
        return controller;
    }

    public void unload() {
        controller = null;
        currentlyFiring = null;
        for (int i = 0; i < guns.size(); i++) {
            guns.get(i).clear();
        }
    }

    public void reloadAllGuns() {
        for (int i = guns.size() - 1; i >= 0; i--) {
            Gun g = guns.get(i);
            if (g != null) {
                g.reloadGun();
                g.finishReloading();
            }
        }
    }

    public boolean checkAmmo(Gun gun, int amount) {
        return InventoryHelper.amtItem(controller.getInventory(), gun.getAmmoType(), gun.getAmmoTypeByte()) >= amount;
    }

    public void removeAmmo(Gun gun, int amount) {
        if (amount == 0)
            return;
        if (gun.isInfiniteAmmo())
            return;

        InventoryHelper.removeItem(controller.getInventory(), gun.getAmmoType(), gun.getAmmoTypeByte(), amount);
    }

    public ItemStack getLastItemHeld() {
        return lastHeldItem;
    }

    public Gun getGun(int typeId) {
        for (int i = guns.size() - 1; i >= 0; i--) {
            Gun check = guns.get(i);
            if (check.getGunType() == typeId)
                return check;
        }

        return null;
    }

    public Gun getGun(String name) {
        for (int i = guns.size() - 1; i >= 0; i--) {
            Gun check = guns.get(i);
            if (check.getName().equalsIgnoreCase(name) || check.getFilename().equalsIgnoreCase(name))
                return check;
        }

        return null;
    }

    public void setLastShot(Gun lastShot) {
        this.lastFiredGun = lastShot;
        this.lastFiredTicks = ticks;
    }

    public void damageByLava(EntityDamageEvent e) {
        if (!takeLavaDamage) {
            e.setCancelled(true);
            return;
        }

        takeLavaDamage = false;
        Sync.get().delay(1).run(() -> PVPGunPlus.resetPlayerDamage(getPlayer(), 0));
        Sync.get().delay(10).run(() -> takeLavaDamage = true);
    }

    public void damageByFire(EntityDamageEvent e) {
        if (!takeLavaDamage)
            e.setCancelled(true);
    }
}

