package com.brawl.oldguns.events;

import com.brawl.oldguns.gun.Explosion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GunExplodeEvent extends GunEvent {

    private final Explosion explosion;

}
