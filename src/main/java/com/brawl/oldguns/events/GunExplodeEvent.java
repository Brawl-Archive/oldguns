package com.brawl.oldguns.events;

import com.brawl.oldguns.gun.Explosion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GunExplodeEvent extends GunEvent {
    private final Explosion explosion;

    public GunExplodeEvent(Explosion explosion) {
        this.explosion = explosion;
    }

}
