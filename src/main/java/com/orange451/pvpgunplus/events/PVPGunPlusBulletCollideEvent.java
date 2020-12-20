package com.orange451.pvpgunplus.events;

import com.orange451.pvpgunplus.gun.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;

public class PVPGunPlusBulletCollideEvent extends PVPGunPlusEvent {
    private final Gun gun;
    private final GunPlayer shooter;
    private final Block blockHit;

    public PVPGunPlusBulletCollideEvent(GunPlayer shooter, Gun gun, Block block) {
        this.gun = gun;
        this.shooter = shooter;
        this.blockHit = block;
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

    public Block getBlockHit() {
        return blockHit;
    }
}
