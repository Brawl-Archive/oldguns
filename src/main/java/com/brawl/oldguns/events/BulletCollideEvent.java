package com.brawl.oldguns.events;

import com.brawl.oldguns.gun.Gun;
import com.brawl.oldguns.gun.GunPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@Getter
@Setter
@AllArgsConstructor
public class BulletCollideEvent extends GunEvent {

    private final GunPlayer shooter;
    private final Gun gun;
    private final Block blockHit;

    public Player getShooterAsPlayer() {
        return shooter.getPlayer();
    }
}
