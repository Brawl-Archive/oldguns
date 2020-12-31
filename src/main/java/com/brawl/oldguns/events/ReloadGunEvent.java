package com.brawl.oldguns.events;

import com.brawl.oldguns.gun.Gun;
import com.brawl.oldguns.gun.GunPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class ReloadGunEvent extends GunEvent {
    private final Gun gun;
    private final GunPlayer shooter;
    private int reloadTime;

    public ReloadGunEvent(GunPlayer shooter, Gun gun) {
        this.gun = gun;
        this.shooter = shooter;
        this.reloadTime = gun.getReloadTime();
    }

    public Player getPlayer() {
        return shooter.getPlayer();
    }
}
