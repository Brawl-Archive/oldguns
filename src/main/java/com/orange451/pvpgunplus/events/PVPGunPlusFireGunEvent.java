package com.orange451.pvpgunplus.events;

import org.bukkit.entity.Player;

import com.orange451.pvpgunplus.gun.Gun;
import com.orange451.pvpgunplus.gun.GunPlayer;

public class PVPGunPlusFireGunEvent extends PVPGunPlusEvent
{
    private Gun gun;
    private GunPlayer shooter;
    private int amountAmmoNeeded;
    private double accuracy;

    public PVPGunPlusFireGunEvent(GunPlayer shooter, Gun gun)
    {
        this.gun = gun;
        this.shooter = shooter;
        this.amountAmmoNeeded = gun.getAmmoAmtNeeded();
        this.accuracy = gun.getAccuracy();
        if (shooter.getPlayer().isSneaking() && gun.getAccuracy_crouched() > -1)
            accuracy = gun.getAccuracy_crouched();
        if (shooter.isAimedIn() && gun.getAccuracy_aimed() > -1)
            accuracy = gun.getAccuracy_aimed();
    }

    public PVPGunPlusEvent setAmountAmmoNeeded(int i)
    {
        this.amountAmmoNeeded = i;
        return this;
    }

    public int getAmountAmmoNeeded()
    {
        return amountAmmoNeeded;
    }

    public double getGunAccuracy()
    {
        return accuracy;
    }

    public Gun getGun()
    {
        return gun;
    }

    public GunPlayer getShooter()
    {
        return shooter;
    }

    public Player getShooterAsPlayer()
    {
        return shooter.getPlayer();
    }

    public void setGunAccuracy(double d)
    {
        this.accuracy = d;
    }
}
