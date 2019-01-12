package com.orange451.pvpgunplus;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import me.signatured.base.particle.Particle;
import me.signatured.base.particle.ParticleType;

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

        Particle particle = new Particle(ParticleType.EXPLOSION_LARGE, location.clone().add(0D, 1D, 0D), new Vector(0.3D, 0.3D, 0.3D), 0.2D, 1);
        particle.sendToWorld(location.getWorld());
    }
}
