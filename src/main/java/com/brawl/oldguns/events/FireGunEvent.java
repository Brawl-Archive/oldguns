package com.brawl.oldguns.events;

import com.brawl.oldguns.gun.Gun;
import com.brawl.oldguns.gun.GunPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class FireGunEvent extends GunEvent {
    private final Gun gun;
    private final GunPlayer shooter;
    private int amountAmmoNeeded;
    private double gunAccuracy;

    public FireGunEvent(GunPlayer shooter, Gun gun) {
        this.gun = gun;
        this.shooter = shooter;
        this.amountAmmoNeeded = gun.getAmmoAmtNeeded();
        this.gunAccuracy = gun.getAccuracy();
        if (shooter.getPlayer().isSneaking() && gun.getAccuracy_crouched() > -1)
            gunAccuracy = gun.getAccuracy_crouched();
        if (shooter.isAimedIn() && gun.getAccuracy_aimed() > -1)
            gunAccuracy = gun.getAccuracy_aimed();
    }

    public GunEvent setAmountAmmoNeeded(int i) {
        this.amountAmmoNeeded = i;
        return this;
    }

    public Player getShooterAsPlayer() {
        return shooter.getPlayer();
    }
}
