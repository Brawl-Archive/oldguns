package com.orange451.pvpgunplus.gun.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * @author Joseph Robert Melsha (joe.melsha@live.com)
 * @link http://www.joemelsha.com
 * @date Jan 19, 2015
 * <p>
 * Copyright 2015 Poofless Gaming, LLC
 */
public class IntVector3 {
    public static final IntVector3 ID = new IntVector3();

    public final int x, y, z;
    private transient int hashCode;
    private transient long longHashCode;

    public IntVector3() {
        x = y = z = 0;
    }

    public IntVector3(Vec3 v) {
        this(v.x, v.y, v.z);
    }

    public IntVector3(Vector v) {
        this(v.getX(), v.getY(), v.getZ());
    }

    public IntVector3(Block b) {
        this(b.getX(), b.getY(), b.getZ());
    }

    public IntVector3(Location l) {
        this(l.getX(), l.getY(), l.getZ());
    }

    public IntVector3(float x, float y, float z) {
        this(x, y, (double) z);
    }

    public IntVector3(double x, double y, double z) {
        this(MCMath.blockInt(x), MCMath.floorInt(y), MCMath.blockInt(z));
    }

    public IntVector3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static IntVector3 rounded(Vector v) {
        return rounded(v.getX(), v.getY(), v.getZ());
    }

    public static IntVector3 rounded(Location l) {
        return rounded(l.getX(), l.getY(), l.getZ());
    }

    public static IntVector3 rounded(Vec3 v) {
        return rounded(v.x, v.y, v.z);
    }

    public static IntVector3 rounded(float x, float y, float z) {
        return rounded(x, y, (double) z);
    }

    public static IntVector3 rounded(double x, double y, double z) {
        return new IntVector3(MCMath.roundInt(x), MCMath.roundInt(y), MCMath.roundInt(z));
    }

    private static int encodeInt(int v) {
        if (v < 0)
            return 0x1 | ((~v) << 1);
        return v << 1;
    }

    public static IntVector3 iv(Location loc) {
        return new IntVector3(loc);
    }

    public static IntVector3 iv(Block block) {
        return new IntVector3(block);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public IntVector3 div(int v) {
        return new IntVector3(x / v, y / v, z / v);
    }

    public IntVector3 add(IntVector3 v) {
        return new IntVector3(x + v.x, y + v.y, z + v.z);
    }

    public IntVector3 sub(IntVector3 v) {
        return new IntVector3(x - v.x, y - v.y, z - v.z);
    }

    public IntVector3 sub(int dx, int dy, int dz) {
        return new IntVector3(x - dx, y - dy, z - dz);
    }

    public IntVector3 add(int dx, int dy, int dz) {
        return new IntVector3(x + dx, y + dy, z + dz);
    }

    public IntVector3 abs() {
        return new IntVector3(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    public IntVector3 sgn() {
        return new IntVector3(Integer.signum(x), Integer.signum(y), Integer.signum(z));
    }

    public Vec3 v() {
        return new Vec3(x, y, z);
    }

    public Vec3 v(int prec) {
        return new Vec3((double) x / prec, (double) y / prec, (double) z / prec);
    }

    public Vec3 vCenter() {
        return new Vec3(x + 0.5, y + 0.5, z + 0.5);
    }

    public Vec3 vCenter(int prec) {
        return new Vec3((double) x / prec + 0.5, (double) y / prec + 0.5, (double) z / prec + 0.5);
    }

    public Vector vec() {
        return new Vector(x, y, z);
    }

    public Vector vec(int prec) {
        return new Vector((double) x / prec, (double) y / prec, (double) z / prec);
    }

    public Vector vecCenter() {
        return new Vector(x + 0.5, y + 0.5, z + 0.5);
    }

    public Vector vecCenter(int prec) {
        return new Vector((double) x / prec + 0.5, (double) y / prec + 0.5, (double) z / prec + 0.5);
    }

    public net.minecraft.server.v1_8_R3.BlockPosition nms() {
        return new net.minecraft.server.v1_8_R3.BlockPosition(x, y, z);
    }

    @Override
    public String toString() {
        return "<" + x + ", " + y + ", " + z + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof IntVector3))
            return false;
        IntVector3 o = (IntVector3) obj;
        return x == o.x && y == o.y && z == o.z;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int x = this.x, y = this.y, z = this.z;
            hashCode = ((encodeInt(x) * 257 + encodeInt(y)) * 257) + encodeInt(z);
        }
        return hashCode;
    }

    public long longHashCode() {
        if (longHashCode == 0) {
            int x = this.x, y = this.y, z = this.z;
            longHashCode = ((encodeInt(x) * 65537L + encodeInt(y)) * 65537L) + encodeInt(z);
        }
        return longHashCode;
    }

    public IntVector3 inv() {
        return new IntVector3(-x, -y, -z);
    }

    public int distSq(IntVector3 o) {
        int dx = x - o.x, dy = y - o.y, dz = z - o.z;
        return dx * dx + dy * dy + dz * dz;
    }
}
