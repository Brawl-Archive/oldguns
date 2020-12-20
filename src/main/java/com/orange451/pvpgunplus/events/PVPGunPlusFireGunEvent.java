package com.orange451.pvpgunplus.events;

import com.orange451.pvpgunplus.gun.*;
import org.bukkit.entity.*;

public class PVPGunPlusFireGunEvent extends PVPGunPlusEvent {
    private final Gun gun;
    private final GunPlayer shooter;
    private int amountAmmoNeeded;
    private double accuracy;

    public PVPGunPlusFireGunEvent(GunPlayer shooter, Gun gun) {
        this.gun = gun;
        this.shooter = shooter;
        this.amountAmmoNeeded = gun.getAmmoAmtNeeded();
        this.accuracy = gun.getAccuracy();
        if (shooter.getPlayer().isSneaking() && gun.getAccuracy_crouched() > -1)
            accuracy = gun.getAccuracy_crouched();
        if (shooter.isAimedIn() && gun.getAccuracy_aimed() > -1)
            accuracy = gun.getAccuracy_aimed();
    }

    public PVPGunPlusEvent setAmountAmmoNeeded(int i) {
        this.amountAmmoNeeded = i;
        return this;
    }

    public int getAmountAmmoNeeded() {
        return amountAmmoNeeded;
    }

    public double getGunAccuracy() {
        return accuracy;
    }

    public void setGunAccuracy(double d) {
        this.accuracy = d;
    }

    public Gun getGun() {
        return gun;
    }

    public GunPlayer getShooter() {
        return shooter;
    }

    public Player getShooterAsPlayer() {
        return shooter.getPlayer();
    }
}
