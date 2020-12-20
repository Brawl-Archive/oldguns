package com.orange451.pvpgunplus.gun;

import org.bukkit.entity.*;

public interface DamageListener {
    void damagePlayer(Player hit, int amount, DamageType type, Player damager);
}
