package com.brawl.oldguns.events;

import com.brawl.oldguns.gun.GunPlayer;
import com.brawl.oldguns.gun.Gun;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;

@Getter
@Setter
@AllArgsConstructor
public class ProjectileDamageEvent extends GunEvent {

    private Gun projectile;
    private GunPlayer shooter;
    private double damage;
    private ProjectileType type;
    private Entity damaged;

    public enum ProjectileType {
        GRENADE, FLASHBANG, MOLOTOV
    }

}
