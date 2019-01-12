package com.orange451.pvpgunplus.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.orange451.pvpgunplus.gun.Gun;
import com.orange451.pvpgunplus.gun.GunPlayer;

public class PVPGunPlusBulletCollideEvent extends PVPGunPlusEvent
{
    private Gun gun;
    private GunPlayer shooter;
    private Block blockHit;

    public PVPGunPlusBulletCollideEvent(GunPlayer shooter, Gun gun, Block block)
    {
        this.gun = gun;
        this.shooter = shooter;
        this.blockHit = block;
    }

    public Gun getGun()
    {
        return gun;
    }

    public GunPlayer getShooter()
    {
        return shooter;
    }

    public Player getShooterAsPlayer()
    {
        return shooter.getPlayer();
    }

    public Block getBlockHit()
    {
        return blockHit;
    }
}
