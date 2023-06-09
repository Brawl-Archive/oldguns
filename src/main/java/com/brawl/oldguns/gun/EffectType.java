package com.brawl.oldguns.gun;

import com.brawl.oldguns.OldGuns;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
@Setter
public class EffectType {
    private final int maxDuration;
    private final Effect type;
    private final double radius;
    private byte specialDat = -1;
    private int duration;
    private Location location;

    public EffectType(int duration, double radius, Effect type) {
        this.duration = duration;
        this.maxDuration = duration;
        this.type = type;
        this.radius = radius;
    }

    public void start(Location location) {
        this.location = location;
        this.duration = this.maxDuration;
        OldGuns.getInstance().addEffect(this);
    }

    public EffectType clone() {
        return new EffectType(this.maxDuration, this.radius, this.type).setSpecialDat(this.specialDat);
    }

    public void tick() {
        this.duration -= 1;

        if (this.duration < 0) {
            OldGuns.getInstance().removeEffect(this);
            return;
        }
        double yRad = this.radius;
        if (this.type.equals(Effect.MOBSPAWNER_FLAMES)) {
            yRad = 0.75D;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if ((player.getWorld().equals(this.location.getWorld())) &&
                        (this.location.distance(player.getLocation()) < this.radius)) {
                    player.setFireTicks(20 * 5);
                }
            }
        }

        for (double i = -this.radius; i <= this.radius; i += 1.0D) {
            for (double ii = -this.radius; ii <= this.radius; ii += 1.0D) {
                for (double iii = 0.0D; iii <= yRad * 2.0D; iii += 1.0D) {
                    int rand = OldGuns.getInstance().getRandom().nextInt(8);
                    if (rand == 2) {
                        Location newloc = this.location.clone().add(i, iii - 1.0D, ii);
                        Location testLoc = this.location.clone().add(0.0D, yRad - 1.0D, 0.0D);
                        if (newloc.distance(testLoc) <= this.radius) {
                            byte dat = (byte) OldGuns.getInstance().getRandom().nextInt(8);
                            if (this.specialDat > -1)
                                dat = this.specialDat;
                            newloc.getWorld().playEffect(newloc, this.type, dat);
                        }
                    }
                }
            }
        }
    }

    public EffectType setSpecialDat(byte specialDat) {
        this.specialDat = specialDat;
        return this;
    }
}