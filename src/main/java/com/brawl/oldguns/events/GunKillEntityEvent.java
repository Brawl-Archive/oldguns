package com.brawl.oldguns.events;

import com.brawl.oldguns.gun.Gun;
import com.brawl.oldguns.gun.GunPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@Getter
@Setter
@AllArgsConstructor
public class GunKillEntityEvent extends GunEvent {

    private final GunPlayer killer;
    private final Gun gun;
    private final Entity killed;

    public Player getKillerAsPlayer() {
        return killer.getPlayer();
    }
}
