package com.orange451.pvpgunplus;

import org.bukkit.Location;
import org.bukkit.Sound;

import com.brawl.base.packets.ParticlePacket;
import com.evogames.compatibility.XEnumParticle;
import com.orange451.pvpgunplus.events.PVPGunPlusBlockExplodeEvent;

import lombok.Getter;

public class PVPGunExplosion
{
	@Getter
    private Location location;

    public PVPGunExplosion(Location location)
    {
        this.location = location;
    }

    public void explode()
    {
        location.getWorld().playSound(location, Sound.EXPLODE, 1, 2);
        ParticlePacket.of(XEnumParticle.EXPLOSION_LARGE).at(location.clone().add(0, 1, 0)).offset(0.3f, 0.3f, 0.3f).data(0.2f).count(1).send();
        
        new PVPGunPlusBlockExplodeEvent(this).callEvent();
    }
    
    public static void callExplosionEvent(Location l) {
    	PVPGunExplosion e = new PVPGunExplosion(l);
    	new PVPGunPlusBlockExplodeEvent(e).callEvent();
    }
}
