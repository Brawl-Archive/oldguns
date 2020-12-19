package com.orange451.pvpgunplus;

import com.brawl.base.packets.ParticlePacket;
import com.brawl.shared.compatibility.XEnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class RaycastHelper {
    public static double PRECISION = 0.1; //Lower is more precise (NEVER SET TO <= 0)
    public static int MAX_RAY_DISTANCE = 256;

    public static Block rayCastToBlock(Location location, Vector normal_direction) {
        Location use = location.clone();
        Block b = location.getBlock();
        Block last = b;
        Vector nvec = normal_direction.multiply(RaycastHelper.PRECISION);

        for (double i = 1; i <= RaycastHelper.MAX_RAY_DISTANCE; i += RaycastHelper.PRECISION) {
            Location check = use.add(nvec);
            Block temp = check.getBlock();
            if (isSolid(temp)) {
                return temp;
            }
            last = temp;
        }
        return b;
    }

    public static Location rayCastToBlockDistance(Location location, Vector normal_direction, boolean temp1) {
        Location use = location.clone();
        Location lastLoc = use.clone();
        Vector nvec = normal_direction.multiply(RaycastHelper.PRECISION);

        for (double i = 1; i <= RaycastHelper.MAX_RAY_DISTANCE; i += RaycastHelper.PRECISION) {
            Location check = use.add(nvec);
            Block temp = check.getBlock();
            if (isSolid(temp)) {
                return lastLoc;
            } else {
                lastLoc = check.clone();
            }

            if (!lastLoc.equals(temp.getLocation()) && temp1) {
                ParticlePacket.of(XEnumParticle.REDSTONE).at(check).count(2).send();
            }
        }
        return lastLoc;
    }

    public static Player rayCastToPlayer(Location location, Vector normal_direction) {
        Player ret = null;
        Location use = location.clone();
        Vector nvec = normal_direction.multiply(RaycastHelper.PRECISION);

        for (double i = 1; i <= RaycastHelper.MAX_RAY_DISTANCE; i += RaycastHelper.PRECISION) {
            Location temp = (use.add(nvec));
            if (i >= 4) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isLocationInPlayer(temp, player)) {
                        return player;
                    }
                }
            }
        }
        return ret;
    }

    private static boolean isLocationInPlayer(Location location, Player player) {
        Location ploc = player.getLocation();
        if (ploc.getWorld().equals(location.getWorld())) {
            if (location.getX() > ploc.getX() - 0.32 && location.getX() < ploc.getX() + 0.32) {
                if (location.getZ() > ploc.getZ() - 0.32 && location.getZ() < ploc.getZ() + 0.32) {
                    return location.getY() > ploc.getY() - 0.1 && location.getY() < ploc.getY() + 2;
                }
            }
        }
        return false;
    }

    public static ArrayList<Entity> getNearbyEntities(Location lastLocation, double dis) {
        ArrayList<Entity> nearby = new ArrayList<Entity>();
        List<Entity> world = lastLocation.getWorld().getEntities();
        synchronized (world) {
            for (int i = world.size() - 1; i >= 0; i--) {
                Entity e = world.get(i);
                if (e != null) {
                    if (e.getLocation().distance(lastLocation) < dis) {
                        nearby.add(e);
                    }
                }
            }
        }
        return nearby;
    }

    public static boolean hasLineOfSight(Location from, Location to) {
        Vector v = to.toVector().subtract(from.toVector()).normalize();
        double dis = rayCastToBlockDistance(from, v, false).distance(from);
        return dis > from.distance(to);
    }

    private static boolean isSolid(Block b) {
        if (b != null && b.getTypeId() > 0)
            return b.getType().isSolid();
        return false;
    }
}
