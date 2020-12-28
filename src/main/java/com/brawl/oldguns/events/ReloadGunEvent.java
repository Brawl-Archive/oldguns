package com.brawl.oldguns.events;

import com.brawl.oldguns.gun.Gun;
import com.brawl.oldguns.gun.GunPlayer;
import org.bukkit.entity.Player;

public class ReloadGunEvent extends GunEvent {
    private final Gun gun;
    private final GunPlayer shooter;
    private int timeForReload;

    public ReloadGunEvent(GunPlayer shooter, Gun gun) {
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
