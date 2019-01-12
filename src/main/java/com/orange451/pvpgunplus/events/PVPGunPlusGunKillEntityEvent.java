package com.orange451.pvpgunplus.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.orange451.pvpgunplus.gun.Gun;
import com.orange451.pvpgunplus.gun.GunPlayer;

public class PVPGunPlusGunKillEntityEvent extends PVPGunPlusEvent
{
    private Gun gun;
    private GunPlayer shooter;
    private Entity shot;

    public PVPGunPlusGunKillEntityEvent(GunPlayer shooter, Gun gun, Entity killed)
    {
        this.gun = gun;
        this.shooter = shooter;
        this.shot = killed;
    }

    public GunPlayer getKiller()
    {
        return shooter;
    }

    public Player getKillerAsPlayer()
    {
        return shooter.getPlayer();
    }

    public Entity getKilled()
    {
        return shot;
    }

    public Gun getGun()
    {
        return gun;
    }
}
