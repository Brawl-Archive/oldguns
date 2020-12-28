package com.brawl.oldguns.events;

import com.brawl.oldguns.gun.Gun;
import com.brawl.oldguns.gun.GunPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class GunKillEntityEvent extends GunEvent {
    private final Gun gun;
    private final GunPlayer shooter;
    private final Entity shot;

    public GunKillEntityEvent(GunPlayer shooter, Gun gun, Entity killed) {
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
