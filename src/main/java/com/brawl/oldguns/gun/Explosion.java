package com.brawl.oldguns.gun;

import com.brawl.base.packets.ParticlePacket;
import com.brawl.oldguns.events.GunExplodeEvent;
import com.brawl.shared.compatibility.XEnumParticle;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;

public class Explosion {
    @Getter
    private final Location location;

    public Explosion(Location location) {
        this.location = location;
    }

    public static void callExplosionEvent(Location l) {
        Explosion e = new Explosion(l);
        new GunExplodeEvent(e).callEvent();
    }

    public void explode() {
        location.getWorld().playSound(location, Sound.EXPLODE, 1, 2);
        ParticlePacket.of(XEnumParticle.EXPLOSION_LARGE).at(location.clone().add(0, 1, 0)).offset(0.3f, 0.3f, 0.3f).data(0.2f).count(1).send();

        new GunExplodeEvent(this).callEvent();
    }
}
