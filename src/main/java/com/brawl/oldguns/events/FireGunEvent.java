package com.brawl.oldguns.events;

import com.brawl.oldguns.gun.Gun;
import com.brawl.oldguns.gun.GunPlayer;
import org.bukkit.entity.Player;

public class FireGunEvent extends GunEvent {
    private final Gun gun;
    private final GunPlayer shooter;
    private int amountAmmoNeeded;
    private double accuracy;

    public FireGunEvent(GunPlayer shooter, Gun gun) {
        this.gun = gun;
        this.shooter = shooter;
        this.amountAmmoNeeded = gun.getAmmoAmtNeeded();
        this.accuracy = gun.getAccuracy();
        if (shooter.getPlayer().isSneaking() && gun.getAccuracy_crouched() > -1)
            accuracy = gun.getAccuracy_crouched();
        if (shooter.isAimedIn() && gun.getAccuracy_aimed() > -1)
            accuracy = gun.getAccuracy_aimed();
    }

    public GunEvent setAmountAmmoNeeded(int i) {
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
