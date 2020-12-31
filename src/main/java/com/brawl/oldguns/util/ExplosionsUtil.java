package com.brawl.oldguns.util;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ExplosionsUtil {

    static final Set<Consumer<CustomExplosion.ExplosionDamageData>> preDamageListeners = new HashSet<>();
    static final Set<Consumer<CustomExplosion.ExplosionDamageData>> postDamageListeners = new HashSet<>();
    static final Set<Consumer<org.bukkit.block.Block>> igniteListeners = new HashSet<>();
    private static final Material[] BAD_BLOCK = {
            Material.DOUBLE_STONE_SLAB2, Material.STONE_SLAB2, Material.STEP,
            Material.DOUBLE_STEP, Material.WOOD_STEP, Material.GOLD_PLATE, Material.IRON_PLATE, Material.STONE_PLATE,
            Material.WOOD_PLATE, Material.ACACIA_STAIRS, Material.BIRCH_WOOD_STAIRS, Material.BRICK_STAIRS,
            Material.COBBLESTONE_STAIRS, Material.DARK_OAK_STAIRS, Material.JUNGLE_WOOD_STAIRS,
            Material.NETHER_BRICK_STAIRS, Material.QUARTZ_STAIRS, Material.RED_SANDSTONE_STAIRS,
            Material.SANDSTONE_STAIRS, Material.SMOOTH_STAIRS, Material.SPRUCE_WOOD_STAIRS, Material.WOOD_STAIRS,
            Material.CARPET, Material.SNOW, Material.TRAP_DOOR, Material.DAYLIGHT_DETECTOR,
            Material.DAYLIGHT_DETECTOR_INVERTED, Material.IRON_TRAPDOOR, Material.DOUBLE_PLANT
    };

    public static void preDamageListener(Consumer<CustomExplosion.ExplosionDamageData> c) {
        preDamageListeners.add(c);
    }

    public static void postDamageListener(Consumer<CustomExplosion.ExplosionDamageData> c) {
        postDamageListeners.add(c);
    }

    public static void igniteListener(Consumer<org.bukkit.block.Block> c) {
        igniteListeners.add(c);
    }

    public static void unregisterListener(Consumer<?> c) {
        igniteListeners.remove(c);
        preDamageListeners.remove(c);
        postDamageListeners.remove(c);
    }

    public static Location getFixedLocation(CustomExplosion explosion) {
        Location loc = new Location(explosion.getWorld().getWorld(), explosion.getPosX(), explosion.getPosY(), explosion.getPosZ());
        boolean isBadBlock = isBadBlock(loc);

        if (isBadBlock) {
            Location above = loc.clone().add(0, 1, 0);

            if (!isBadBlock(above) && above.getBlock().getType().isTransparent())
                return above;
        }

        return loc;
    }

    private static boolean isBadBlock(Location loc) {
        for (Material m : BAD_BLOCK) {
            if (loc.getBlock().getType() == m) {
                return true;
            }
        }

        return false;
    }
}