package com.orange451.pvpgunplus;

import com.brawl.base.packets.*;
import com.brawl.shared.compatibility.*;
import com.orange451.pvpgunplus.events.*;
import lombok.*;
import org.bukkit.*;

public class PVPGunExplosion {
    @Getter
    private final Location location;

    public PVPGunExplosion(Location location) {
        this.location = location;
    }

    public static void callExplosionEvent(Location l) {
        PVPGunExplosion e = new PVPGunExplosion(l);
        new PVPGunPlusBlockExplodeEvent(e).callEvent();
    }

    public void explode() {
        location.getWorld().playSound(location, Sound.EXPLODE, 1, 2);
        ParticlePacket.of(XEnumParticle.EXPLOSION_LARGE).at(location.clone().add(0, 1, 0)).offset(0.3f, 0.3f, 0.3f).data(0.2f).count(1).send();

        new PVPGunPlusBlockExplodeEvent(this).callEvent();
    }
}
