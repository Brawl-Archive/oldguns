package com.orange451.pvpgunplus.gun;

import org.bukkit.entity.Player;

public interface DamageListener {
    void damagePlayer(Player hit, int amount, DamageType type, Player damager);
}
