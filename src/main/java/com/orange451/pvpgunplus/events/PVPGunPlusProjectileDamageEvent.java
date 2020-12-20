package com.orange451.pvpgunplus.events;

import com.orange451.pvpgunplus.gun.*;
import lombok.*;
import org.bukkit.entity.*;

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
