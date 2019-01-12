package com.orange451.pvpgunplus.events;

import org.bukkit.entity.Player;

import com.orange451.pvpgunplus.gun.Gun;
import com.orange451.pvpgunplus.gun.GunPlayer;

public class PVPGunPlusReloadGunEvent extends PVPGunPlusEvent
{
    private Gun gun;
    private GunPlayer shooter;
    private int timeForReload;

    public PVPGunPlusReloadGunEvent(GunPlayer shooter, Gun gun)
    {
        this.gun = gun;
        this.shooter = shooter;
        this.timeForReload = gun.getReloadTime();
    }

    public Gun getGun()
    {
        return gun;
    }

    public GunPlayer getGunPlayer()
    {
        return shooter;
    }

    public Player getPlayer()
    {
        return shooter.getPlayer();
    }

    public void setReloadTime(int d)
    {
        this.timeForReload = d;
    }

    public int getReloadTime()
    {
        return this.timeForReload;
    }
}
