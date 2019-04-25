package com.orange451.pvpgunplus;

import com.brawl.base.packets.ParticlePacket;
import com.evogames.compatibility.XEnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;

public class PVPGunExplosion
{
    private Location location;

    public PVPGunExplosion(Location location)
    {
        this.location = location;
    }

    public void explode()
    {
        location.getWorld().playSound(location, Sound.EXPLODE, 1, 2);

        ParticlePacket.of(XEnumParticle.EXPLOSION_LARGE).at(location.clone().add(0, 1, 0)).offset(0.3f, 0.3f, 0.3f).data(0.2f).count(1).send();
    }
}
