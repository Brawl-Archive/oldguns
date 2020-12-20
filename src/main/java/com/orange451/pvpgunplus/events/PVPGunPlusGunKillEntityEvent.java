package com.orange451.pvpgunplus.events;

import com.orange451.pvpgunplus.gun.*;
import org.bukkit.entity.*;

public class PVPGunPlusGunKillEntityEvent extends PVPGunPlusEvent {
    private final Gun gun;
    private final GunPlayer shooter;
    private final Entity shot;

    public PVPGunPlusGunKillEntityEvent(GunPlayer shooter, Gun gun, Entity killed) {
        this.gun = gun;
        this.shooter = shooter;
        this.shot = killed;
    }

    public GunPlayer getKiller() {
        return shooter;
    }

    public Player getKillerAsPlayer() {
        return shooter.getPlayer();
    }

    public Entity getKilled() {
        return shot;
    }

    public Gun getGun() {
        return gun;
    }
}
