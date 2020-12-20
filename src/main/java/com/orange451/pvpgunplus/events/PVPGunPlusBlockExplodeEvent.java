package com.orange451.pvpgunplus.events;

import com.orange451.pvpgunplus.*;
import lombok.*;

@Getter
public class PVPGunPlusBlockExplodeEvent extends PVPGunPlusEvent {
    private final PVPGunExplosion explosion;

    public PVPGunPlusBlockExplodeEvent(PVPGunExplosion explosion) {
        this.explosion = explosion;
    }

}
