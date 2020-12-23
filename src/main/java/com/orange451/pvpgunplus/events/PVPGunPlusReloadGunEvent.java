package com.orange451.pvpgunplus.events;

import com.orange451.pvpgunplus.gun.Gun;
import com.orange451.pvpgunplus.gun.GunPlayer;
import org.bukkit.entity.Player;

public class PVPGunPlusReloadGunEvent extends PVPGunPlusEvent {
    private final Gun gun;
    private final GunPlayer shooter;
    private int timeForReload;

    public PVPGunPlusReloadGunEvent(GunPlayer shooter, Gun gun) {
        this.gun = gun;
        this.shooter = shooter;
        this.timeForReload = gun.getReloadTime();
    }

    public Gun getGun() {
        return gun;
    }

    public GunPlayer getGunPlayer() {
        return shooter;
    }

    public Player getPlayer() {
        return shooter.getPlayer();
    }

    public int getReloadTime() {
        return this.timeForReload;
    }

    public void setReloadTime(int d) {
        this.timeForReload = d;
    }
}
