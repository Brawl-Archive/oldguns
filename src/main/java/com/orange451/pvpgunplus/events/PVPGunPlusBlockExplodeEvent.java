package com.orange451.pvpgunplus.events;

import com.orange451.pvpgunplus.PVPGunExplosion;

import lombok.Getter;

@Getter
public class PVPGunPlusBlockExplodeEvent extends PVPGunPlusEvent
{
    private PVPGunExplosion explosion;

    public PVPGunPlusBlockExplodeEvent(PVPGunExplosion explosion)
    {
    	this.explosion = explosion;
    }

}
