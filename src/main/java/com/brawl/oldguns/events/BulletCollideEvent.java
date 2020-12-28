package com.brawl.oldguns.events;

import com.brawl.oldguns.gun.Gun;
import com.brawl.oldguns.gun.GunPlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BulletCollideEvent extends GunEvent {
    private final Gun gun;
    private final GunPlayer shooter;
    private final Block blockHit;

    public BulletCollideEvent(GunPlayer shooter, Gun gun, Block block) {
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
