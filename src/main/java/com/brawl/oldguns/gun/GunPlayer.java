package com.brawl.oldguns.gun;

import com.brawl.base.util.BaseMeta;
import com.brawl.base.util.scheduler.Sync;
import com.brawl.oldguns.OldGuns;
import com.brawl.oldguns.util.InventoryHelper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.UUID;

@Getter
@Setter
public class GunPlayer {

    public static BaseMeta<GunPlayer, OldGuns> meta;
    private static Integer GUN_SWAP_COOLDOWN = null;
    private final ArrayList<Gun> guns;
    private final int gunSwapCooldown;
    private long lastHit;
    private Location lastHitLocation;
    private GunPlayer lastHitSource;
    private boolean takeLavaDamage = true;
    private int ticks;
    private Player player;
    private ItemStack lastHeldItem;
    private Gun currentlyFiring;
    private Gun lastFiredGun;
    private int lastFiredTicks;
    private boolean enabled = true;

    public GunPlayer(OldGuns plugin, Player player) {
        this.player = player;
        this.guns = plugin.getLoadedGuns();
        for (Gun gun : guns) {
            gun.setOwner(this);
        }

        if (GUN_SWAP_COOLDOWN == null)
            GUN_SWAP_COOLDOWN = plugin.getConfig().getInt("gunswapcooldown", 0);

        gunSwapCooldown = GUN_SWAP_COOLDOWN;
    }

    public static void initMeta() {
        if (meta != null)
            return;
        meta = new BaseMeta<>(OldGuns.getInstance(), p -> new GunPlayer(OldGuns.getInstance(), p));
    }

    public static GunPlayer get(Player player) {
        synchronized (meta) {
            return meta.get(player);
        }
    }

    public static GunPlayer get(UUID uuid) {
        synchronized (meta) {
            return meta.get(uuid);
        }
    }

    public boolean isAimedIn() {
        if (player == null)
            return false;
        if (!player.isOnline())
            return false;

        return player.hasPotionEffect(PotionEffectType.SLOW);
    }

    public boolean canShoot(Gun gun) {
        return !(lastFiredGun != null && lastFiredGun != gun && ticks - lastFiredTicks <= gunSwapCooldown);
    }

    public boolean onClick(String clickType, Projectile alreadyFired) {
        if (!enabled)
            return false;

        Gun holding = null;
        ItemStack hand = player.getItemInHand();
        if (hand != null) {
            ArrayList<Gun> tempgun = this.getGunsByType(hand);
            ArrayList<Gun> canFire = new ArrayList<>();
            for (Gun gun : tempgun) {
                if (/*PermissionInterface.checkPermission(controller, gun.node) ||*/!gun.isNeedsPermission()) {
                    canFire.add(gun);
                }
            }
            if (tempgun.size() > canFire.size() && canFire.size() == 0) {
                if (tempgun.get(0).getPermissionMessage() != null && tempgun.get(0).getPermissionMessage().length() > 0)
                    player.sendMessage(tempgun.get(0).getPermissionMessage());
                return false;
            }
            tempgun.clear();
            for (Gun check : canFire) {
                byte gunDat = check.getGunByte();
                byte itmDat = hand.getData().getData();

                if (gunDat == itmDat || check.isIgnoreItemData())
                    holding = check;
            }
            canFire.clear();
        }
        if (holding != null) {
            String shootAction = "right";
            if (holding.getProjType().replace(" ", "").replace("_", "").equals("crossbow"))
                shootAction = "bow";
            if ((holding.isCanClickRight() || holding.isCanAimRight()) && clickType.equals(shootAction)) {
                if (!holding.isCanAimRight()) {
                    holding.setHeldDownTicks(holding.getHeldDownTicks() + 1);
                    holding.setLastFired(0);
                    if (currentlyFiring == null) {
                        fireGun(holding, alreadyFired);
                    }
                } else {
                    checkAim();
                }
            } else if ((holding.isCanClickLeft() || holding.isCanAimLeft()) && clickType.equals("left")) {
                if (!holding.isCanAimLeft()) {
                    holding.setHeldDownTicks(0);
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
            player.removePotionEffect(PotionEffectType.SLOW);
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 60 * 10, 4));
        }
    }

    private void fireGun(Gun gun, Projectile alreadyFired) {
        if (/*PermissionInterface.checkPermission(controller, gun.node) ||*/!gun.isNeedsPermission()) {
            if (gun.getTimer() <= 0) {

                if (alreadyFired != null) {
                    Bullet b = new Bullet(this, alreadyFired.getVelocity(), gun, alreadyFired);
                    OldGuns.getInstance().addBullet(b);
                    this.removeAmmo(gun, gun.getAmmoAmtNeeded() - 1);
                } else {
                    this.currentlyFiring = gun;
                    gun.setFiring(true);
                }

                lastFiredTicks = ticks;
                lastFiredGun = gun;

            }
        } else {
            if (gun.getPermissionMessage() != null && gun.getPermissionMessage().length() > 0)
                player.sendMessage(gun.getPermissionMessage());
        }
    }

    public void tick() {
        ticks++;
        if (enabled && player != null) {
            renameGuns(player);
            ItemStack hand = player.getItemInHand();
            this.lastHeldItem = hand;
            if (ticks % 10 == 0 && hand != null) {
                Gun g = OldGuns.getInstance().getGun(hand.getTypeId());
                if (g == null) {
                    player.removePotionEffect(PotionEffectType.SLOW);
                }
            }
            for (Gun g : guns) {
                if (g != null) {
                    g.tick();

                    if (player.isDead()) {
                        g.finishReloading();
                    }

                    if (hand != null) {
                        if (g.getGunType() == hand.getTypeId()) {
                            if (this.isAimedIn() && !g.isCanAimLeft() && !g.isCanAimRight()) {
                                player.removePotionEffect(PotionEffectType.SLOW);
                            }
                        }
                    }

                    if (currentlyFiring != null && g.getTimer() <= 0 && currentlyFiring.equals(g))
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
        for (ItemStack item : items) {
            if (item != null) {
                String name = getGunName(item);
                if (name != null && name.length() > 0) {
                    setName(item, name);
                }
            }
        }
    }

    public ArrayList<Gun> getGunsByType(ItemStack item) {
        ArrayList<Gun> ret = new ArrayList<>();
        for (Gun gun : guns) {
            if (gun.getGunMaterial().equals(item.getType())) {
                ret.add(gun);
            }
        }

        return ret;
    }

    public String getGunName(org.bukkit.inventory.ItemStack item) {
        String ret = "";
        ArrayList<Gun> tempgun = getGunsByType(item);
        int amtGun = tempgun.size();
        if (amtGun > 0) {
            for (Gun current : tempgun) {
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
        if (current.isHasClip()) {
            int leftInClip;
            int ammoLeft;
            int maxInClip = current.getMaxClipSize();

            int currentAmmo = (int) Math.floor(InventoryHelper.amtItem(player.getInventory(), current.getAmmoType(), current.getAmmoByte()) / ((double) current.getAmmoAmtNeeded()));
            ammoLeft = currentAmmo - maxInClip + current.getRoundsFired();
            if (ammoLeft < 0)
                ammoLeft = 0;
            leftInClip = currentAmmo - ammoLeft;
            add = ChatColor.YELLOW + "    «" + leftInClip + " │ " + (current.isInfiniteAmmo() ? "∞" : Integer.toString(ammoLeft)) + "»";
            if (current.isReloading()) {
                int reloadSize = 4;
                double reloadFrac = ((current.getReloadTime() - current.getGunReloadTimer()) / (double) current.getReloadTime());
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
        String name = current.getGunName();
        return name + add;
    }

    public org.bukkit.inventory.ItemStack setName(org.bukkit.inventory.ItemStack item, String name) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        item.setItemMeta(im);

        return item;
    }

    public void unload() {
        player = null;
        currentlyFiring = null;
        for (Gun gun : guns) {
            gun.clear();
        }
    }

    public void reloadAllGuns() {
        for (Gun g : guns) {
            if (g != null) {
                g.reloadGun();
                g.finishReloading();
            }
        }
    }

    public boolean checkAmmo(Gun gun, int amount) {
        return InventoryHelper.amtItem(player.getInventory(), gun.getAmmoType(), gun.getAmmoByte()) >= amount;
    }

    public void removeAmmo(Gun gun, int amount) {
        if (amount == 0)
            return;
        if (gun.isInfiniteAmmo())
            return;

        InventoryHelper.removeItem(player.getInventory(), gun.getAmmoType(), gun.getAmmoByte(), amount);
    }

    public Gun getGun(int typeId) {
        for (Gun g : guns) {
            if (g.getGunType() == typeId)
                return g;
        }

        return null;
    }

    public Gun getGun(String name) {
        for (Gun g : guns) {
            if (g.getGunName().equalsIgnoreCase(name) || g.getFileName().equalsIgnoreCase(name))
                return g;
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
        Sync.get().delay(1).run(() -> OldGuns.resetPlayerDamage(getPlayer(), 0));
        Sync.get().delay(10).run(() -> takeLavaDamage = true);
    }

    public void damageByFire(EntityDamageEvent e) {
        if (!takeLavaDamage)
            e.setCancelled(true);
    }
}

