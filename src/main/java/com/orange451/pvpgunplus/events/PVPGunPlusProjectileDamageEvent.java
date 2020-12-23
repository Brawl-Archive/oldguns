package com.orange451.pvpgunplus.events;

import com.orange451.pvpgunplus.gun.Gun;
import com.orange451.pvpgunplus.gun.GunPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;

@Getter
@Setter
@AllArgsConstructor
public class PVPGunPlusProjectileDamageEvent extends PVPGunPlusEvent {

    private Gun projectile;
    private GunPlayer shooter;
    private double damage;
    private ProjectileType type;
    private Entity damaged;

    public enum ProjectileType {
        GRENADE, FLASHBANG, MOLOTOV
    }

}
