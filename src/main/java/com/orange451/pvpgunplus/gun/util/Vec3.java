package com.orange451.pvpgunplus.gun.util;

import com.brawl.util.*;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.bukkit.util.*;

import java.util.*;

/**
 * @author Joseph Robert Melsha (joe.melsha@live.com)
 * @link http://www.joemelsha.com
 * @date Oct 10, 2015
 * <p>
 * Copyright 2015 Poofless Gaming, LLC
 */
public class Vec3 {
	public static final Vec3 ID = new Vec3(0, 0, 0);

	public static final double EPSILON = 1E-6;

	public final double x, y, z;

	public Vec3(double x, double y, double z) {
		if (x == -0)
			x = 0;
		if (y == -0)
			y = 0;
		if (z == -0)
			z = 0;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3(double[] array) {
		double x = 0, y = 0, z = 0;
		if (array != null)
			switch (array.length) {
				case 0:
					break;
				case 1:
					x = array[0]; //???
					break;
				case 2:
					x = array[0];
					z = array[1];
					break;
				default:
					x = array[0];
					y = array[1];
					z = array[2];
					break;
			}
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static Vec3 angles(Location l) {
		return new Vec3(0, l.getPitch(), l.getYaw());
	}

	public static Vec3 anglesToDir(Location l) {
		return anglesToDir(l.getYaw(), l.getPitch());
	}

	public static Vec3 anglesToDir(double yaw, double pitch) {
		double pitchTheta = Math.toRadians(pitch);
		double yawTheta = Math.toRadians(yaw);

		double xz = Math.cos(pitchTheta);

		double x = -xz * Math.sin(yawTheta);
		double y = -Math.sin(pitchTheta);
		double z = xz * Math.cos(yawTheta);
		return new Vec3(x, y, z);
	}

	public double sum() {
		return x + y + z;
	}

	public IntVector3 iv() {
		return new IntVector3(this);
	}

	public int blockX() {
		return MCMath.blockInt(x);
	}

	public int blockY() {
		return MCMath.floorInt(y);
	}

	public int blockZ() {
		return MCMath.blockInt(z);
	}

	public BlockPosition pos() {
		return new BlockPosition(blockX(), blockY(), blockZ());
	}

	public Block block(World w) {
		return w.getBlockAt(blockX(), blockY(), blockZ());
	}

	public Vec3 dir() {
		//TODO: roll
		return anglesToDir(z, y);
	}

	/**
	 * roll, pitch, yaw
	 */
	public Vec3 angles() {
		double yaw, pitch;
		if (x == 0 && z == 0) {
			yaw = 0;
			if (y > 0)
				pitch = -90;
			else if (y < 0)
				pitch = 90;
			else
				pitch = 0;
		} else {
			yaw = Math.toDegrees(MathUtil.atan2(-x, z));
			pitch = Math.toDegrees(MathUtil.atan(-y / MathUtil.sqrt_fast(z * z + x * x)));
		}
		return new Vec3(0, pitch, yaw); //roll will always be 0 because it is indeterminable
	}

	//TODO: add function to change roll.

	public double pitch() {
		double pitch;
		if (x == 0 && z == 0) {
			if (y > 0)
				pitch = -90;
			else if (y < 0)
				pitch = 90;
			else
				pitch = 0;
		} else
			pitch = Math.toDegrees(MathUtil.atan(-y / MathUtil.sqrt_fast(z * z + x * x)));
		return pitch;
	}

	public double yaw() {
		double yaw;
		if (x == 0 && z == 0)
			yaw = 0;
		else
			yaw = Math.toDegrees(MathUtil.atan2(-x, z));
		return yaw;
	}

	public Vector v() {
		return new Vector(x, y, z);
	}

	public Location l(World w) {
		return l(w, 0, 0);
	}

	public Location l(World w, Vector dir) {
		return l(w, v(dir));
	}

	public Location l(World w, Vec3 dir) {
		Vec3 angles = dir.angles();
		return l(w, angles.z, angles.y);
	}

	public Location l(World w, double yaw, double pitch) {
		return new Location(w, x, y, z, (float) yaw, (float) pitch);
	}

	//	public Vec3 apply(Matrix4 m) {
	//		return m.mul(this);
	//	}

	/**
	 * @return uniformly random unit vector, 3D
	 */
	public static Vec3 random(Random r) {
		double theta = r.nextDouble() * 2 * Math.PI;
		double phi = Math.acos(2 * r.nextDouble() - 1);
		double sin_theta = Math.sin(theta);
		double cos_theta = Math.cos(theta);
		double sin_phi = Math.sin(phi);
		double cos_phi = Math.cos(phi);
		double x = sin_phi * cos_theta;
		double y = cos_phi;
		double z = sin_phi * sin_theta;
		return new Vec3(x, y, z);
	}

	/**
	 * @return uniformly random unit vector, 2D (x, z)
	 */
	public static Vec3 random_xz(Random r) {
		double theta = r.nextDouble() * 2 * Math.PI;
		double x = Math.cos(theta);
		double y = 0;
		double z = Math.sin(theta);
		return new Vec3(x, y, z);
	}

	//unary ops
	public double[] array() {
		return new double[] { x, y, z };
	}

	public Vec3 floor() {
		return new Vec3(Math.floor(x), Math.floor(y), Math.floor(z));
	}

	public Vec3 ceil() {
		return new Vec3(Math.ceil(x), Math.ceil(y), Math.ceil(z));
	}

	public Vec3 sgn() {
		return new Vec3(Math.signum(x), Math.signum(y), Math.signum(z));
	}

	public Vec3 neg() {
		return new Vec3(-x, -y, -z);
	}

	public Vec3 abs() {
		return new Vec3(Math.abs(x), Math.abs(y), Math.abs(z));
	}

	public Vec3 norm() {
		double len = len();
		if (len <= EPSILON)
			return ID;
		return div(len);
	}

	public double lenSq() {
		return lenSq(x, y, z);
	}

	public static double lenSq(double x, double y, double z) {
		return x * x + y * y + z * z;
	}

	public static double len(double x, double y, double z) {
		return MathUtil.sqrt_fast(lenSq(x, y, z));
	}

	public double len() {
		return len(x, y, z);
	}

	public double avg() {
		return avg(x, y, z);
	}

	public static double avg(double x, double y, double z) {
		return (x + y + z) / 3;
	}

	public double min() {
		return singleMin(x, y, z);
	}

	@SuppressWarnings("SuspiciousNameCombination")
	public static double singleMin(double x, double y, double z) {
		if (x > y)
			x = y;
		if (x > z)
			x = z;
		return x;
	}

	public double max() {
		return singleMax(x, y, z);
	}

	@SuppressWarnings("SuspiciousNameCombination")
	public static double singleMax(double x, double y, double z) {
		if (x < y)
			x = y;
		if (x < z)
			x = z;
		return x;
	}

	public Vec3 ordered() {
		double ox = x, oy = y, oz = z;
		if (oz < ox) {
			double tmp = oz;
			oz = ox;
			ox = tmp;
		}
		if (oz < oy) {
			double tmp = oz;
			oz = oy;
			oy = tmp;
		}
		if (oy < ox) {
			double tmp = oy;
			oy = ox;
			ox = tmp;
		}
		return new Vec3(ox, oy, oz);
	}

	public double range() {
		return max() - min();
	}

	public double x() {
		return x;
	}

	public double y() {
		return y;
	}

	public double z() {
		return z;
	}

	//binary ops

	public Vec3 x(double x) {
		return new Vec3(x, y, z);
	}

	public Vec3 y(double y) {
		return new Vec3(x, y, z);
	}

	public Vec3 z(double z) {
		return new Vec3(x, y, z);
	}

	public Vec3 add(double n) {
		return new Vec3(x + n, y + n, z + n);
	}

	public Vec3 addX(double dx) {
		return new Vec3(x + dx, y, z);
	}

	public Vec3 addY(double dy) {
		return new Vec3(x, y + dy, z);
	}

	public Vec3 addZ(double dz) {
		return new Vec3(x, y, z + dz);
	}

	public Vec3 sub(double n) {
		return new Vec3(x - n, y - n, z - n);
	}

	public Vec3 mul(double n) {
		return new Vec3(x * n, y * n, z * n);
	}

	public Vec3 div(double n) {
		return new Vec3(x / n, y / n, z / n);
	}

	public Vec3 mod(double n) {
		return new Vec3(x % n, y % n, z % n);
	}

	public Vec3 pow(double n) {
		return new Vec3(Math.pow(x, n), Math.pow(y, n), Math.pow(z, n));
	}

	public Vec3 exp(double n) {
		return new Vec3(Math.pow(n, x), Math.pow(n, y), Math.pow(n, z));
	}

	//pitch first!
	public Vec3 pitch(double pitch) {
		double pt = Math.toRadians(-pitch);
		double pc = Math.cos(pt);
		double ps = Math.sin(pt);
		double x, y, z;
		x = this.x;
		y = this.y * pc + this.z * ps;
		z = this.z * pc - this.y * ps;
		return new Vec3(x, y, z);
	}

	public Vec3 yaw(double yaw) {
		double yt = Math.toRadians(-yaw);
		double yc = Math.cos(yt);
		double ys = Math.sin(yt);
		double x, y, z;
		x = this.x * yc + this.z * ys;
		y = this.y;
		z = this.z * yc - this.x * ys;
		return new Vec3(x, y, z);
	}

	public Vec3 rot(Vector dir) {
		return rot(dir.getZ(), dir.getY());
	}

	public Vec3 rot(Vec3 dir) {
		return rot(dir.z, dir.y);
	}

	public Vec3 rot(double yaw, double pitch) {
		double pt = Math.toRadians(pitch);
		double pc = Math.cos(pt);
		double ps = Math.sin(pt);
		double x, y, z;
		x = this.x;
		y = this.y * pc + this.z * ps;
		z = this.z * pc - this.y * ps;

		double yt = Math.toRadians(-yaw);
		double yc = Math.cos(yt);
		double ys = Math.sin(yt);
		x = x * yc + z * ys;
		z = z * yc - x * ys;
		return new Vec3(x, y, z);
	}

	//vec ops
	public double dot(Location o) {
		return dot(o.getX(), o.getY(), o.getZ());
	}

	public double dot(Vector o) {
		return dot(o.getX(), o.getY(), o.getZ());
	}

	public double dot(Vec3 o) {
		return dot(o.x, o.y, o.z);
	}

	public double dot(double ox, double oy, double oz) {
		return x * ox + y * oy + z * oz;
	}

	public Vec3 cross(Location o) {
		return cross(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 cross(Vector o) {
		return cross(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 cross(Vec3 o) {
		return cross(o.x, o.y, o.z);
	}

	public Vec3 cross(double ox, double oy, double oz) {
		return new Vec3(y * oz - oy * z, z * ox - oz * x, x * oy - ox * y);
	}

	public double angle(Location o) {
		return angle(o.getX(), o.getY(), o.getZ());
	}

	public double angle(Vector o) {
		return angle(o.getX(), o.getY(), o.getZ());
	}

	public double angle(Vec3 o) {
		return angle(o.x, o.y, o.z);
	}

	public double angle(double ox, double oy, double oz) {
		double dot = dot(ox, oy, oz) / (len() * len(ox, oy, oz));
		return Math.acos(dot);
	}

	public Vec3 mid(Location o) {
		return mid(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 mid(Vector o) {
		return mid(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 mid(Vec3 o) {
		return mid(o.x, o.y, o.z);
	}

	public Vec3 mid(double ox, double oy, double oz) {
		return new Vec3((x + ox) / 2, (y + oy) / 2, (z + oz) / 2);
	}

	public Vec3 add(Location o) {
		return add(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 add(Vector o) {
		return add(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 add(Vec3 o) {
		return add(o.x, o.y, o.z);
	}

	public Vec3 add(double ox, double oy, double oz) {
		return new Vec3(x + ox, y + oy, z + oz);
	}

	public Vec3 sub(Location o) {
		return sub(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 sub(Vector o) {
		return sub(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 sub(Vec3 o) {
		return sub(o.x, o.y, o.z);
	}

	public Vec3 sub(double ox, double oy, double oz) {
		return new Vec3(x - ox, y - oy, z - oz);
	}

	public Vec3 subX(double ox) {
		return new Vec3(x - ox, y, z);
	}

	public Vec3 subY(double oy) {
		return new Vec3(x, y - oy, z);
	}

	public Vec3 subZ(double oz) {
		return new Vec3(x, y, z - oz);
	}

	public Vec3 mul(Location o) {
		return mul(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 mul(Vector o) {
		return mul(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 mul(Vec3 o) {
		return mul(o.x, o.y, o.z);
	}

	public Vec3 mul(double ox, double oy, double oz) {
		return new Vec3(x * ox, y * oy, z * oz);
	}

	public Vec3 mulX(double ox) {
		return new Vec3(x * ox, y, z);
	}

	public Vec3 mulY(double oy) {
		return new Vec3(x, y * oy, z);
	}

	public Vec3 mulZ(double oz) {
		return new Vec3(x, y, z * oz);
	}

	public Vec3 div(Location o) {
		return div(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 div(Vector o) {
		return div(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 div(Vec3 o) {
		return div(o.x, o.y, o.z);
	}

	public Vec3 div(double ox, double oy, double oz) {
		return new Vec3(x / ox, y / oy, z / oz);
	}

	public Vec3 divX(double ox) {
		return new Vec3(x / ox, y, z);
	}

	public Vec3 divY(double oy) {
		return new Vec3(x, y / oy, z);
	}

	public Vec3 divZ(double oz) {
		return new Vec3(x, y, z / oz);
	}

	public Vec3 pow(Location o) {
		return pow(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 pow(Vector o) {
		return pow(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 pow(Vec3 o) {
		return pow(o.x, o.y, o.z);
	}

	public Vec3 pow(double ox, double oy, double oz) {
		return new Vec3(Math.pow(x, ox), Math.pow(y, oy), Math.pow(z, oz));
	}

	public Vec3 powX(double ox) {
		return new Vec3(Math.pow(x, ox), y, z);
	}

	public Vec3 powY(double oy) {
		return new Vec3(x, Math.pow(y, oy), z);
	}

	public Vec3 powZ(double oz) {
		return new Vec3(x, y, Math.pow(z, oz));
	}

	public Vec3 exp(Location o) {
		return exp(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 exp(Vector o) {
		return exp(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 exp(Vec3 o) {
		return exp(o.x, o.y, o.z);
	}

	public Vec3 exp(double ox, double oy, double oz) {
		return new Vec3(Math.pow(ox, x), Math.pow(oy, y), Math.pow(oz, z));
	}

	public Vec3 expX(double ox) {
		return new Vec3(Math.pow(ox, x), y, z);
	}

	public Vec3 expY(double oy) {
		return new Vec3(x, Math.pow(oy, y), z);
	}

	public Vec3 expZ(double oz) {
		return new Vec3(x, y, Math.pow(oz, z));
	}

	public Vec3 mod(Location o) {
		return mod(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 mod(Vector o) {
		return mod(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 mod(Vec3 o) {
		return mod(o.x, o.y, o.z);
	}

	public Vec3 mod(double ox, double oy, double oz) {
		return new Vec3(x % ox, y % oy, z % oz);
	}

	public Vec3 modX(double ox) {
		return new Vec3(x % ox, y, z);
	}

	public Vec3 modY(double oy) {
		return new Vec3(x, y % oy, z);
	}

	public Vec3 modZ(double oz) {
		return new Vec3(x, y, z % oz);
	}

	public Vec3 min(Vector o) {
		return min(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 min(Vec3 o) {
		return min(o.x, o.y, o.z);
	}

	public Vec3 min(double ox, double oy, double oz) {
		return new Vec3(Math.min(x, ox), Math.min(y, oy), Math.min(z, oz));
	}

	public Vec3 max(Location o) {
		return max(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 max(Vector o) {
		return max(o.getX(), o.getY(), o.getZ());
	}

	public Vec3 max(Vec3 o) {
		return max(o.x, o.y, o.z);
	}

	public Vec3 max(double ox, double oy, double oz) {
		return new Vec3(Math.max(x, ox), Math.max(y, oy), Math.max(z, oz));
	}

	public double distSq(Location o) {
		return distSq(o.getX(), o.getY(), o.getZ());
	}

	public double distSq(Vector o) {
		return distSq(o.getX(), o.getY(), o.getZ());
	}

	public double distSq(Vec3 o) {
		return distSq(o.x, o.y, o.z);
	}

	public double distSq(double ox, double oy, double oz) {
		ox -= x;
		oy -= y;
		oz -= z;
		return ox * ox + oy * oy + oz * oz;
	}

	public double dist(Location o) {
		return dist(o.getX(), o.getY(), o.getZ());
	}

	public double dist(Vector o) {
		return dist(o.getX(), o.getY(), o.getZ());
	}

	public double dist(Vec3 o) {
		return dist(o.x, o.y, o.z);
	}

	public double dist(double ox, double oy, double oz) {
		return MathUtil.sqrt_fast(distSq(ox, oy, oz));
	}

	//equals
	public boolean equals(Vec3 o, double epsilon) {
		return equals(o.x, o.y, o.z, epsilon);
	}

	public boolean equals(double ox, double oy, double oz, double epsilon) {
		long lx1 = MathUtil.bits(x, epsilon);
		long lx2 = MathUtil.bits(ox, epsilon);
		if (lx1 != lx2)
			return false;
		long ly1 = MathUtil.bits(y, epsilon);
		long ly2 = MathUtil.bits(oy, epsilon);
		if (ly1 != ly2)
			return false;
		long lz1 = MathUtil.bits(z, epsilon);
		long lz2 = MathUtil.bits(oz, epsilon);
		if (lz1 != lz2)
			return false;
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Vec3))
			return false;
		return equals((Vec3) o, EPSILON);
	}

	private transient int hashCode;

	@Override
	public int hashCode() {
		if (hashCode == 0)
			hashCode = hashCode(EPSILON);
		return hashCode;
	}

	public int hashCode(double epsilon) {
		long lx = MathUtil.bits(x, epsilon);
		long ly = MathUtil.bits(y, epsilon);
		long lz = MathUtil.bits(z, epsilon);
		return LookupHash.combine(0x703edfdb, (int) (lx >>> 32), (int) lx, (int) (ly >>> 32), (int) ly, (int) (lz >>> 32), (int) lz);
	}

	@Override
	public String toString() {
		return "<" + x + ", " + y + ", " + z + ">";
	}

	public static Vec3 v(Vector v) {
		return new Vec3(v.getX(), v.getY(), v.getZ());
	}

	public static Vec3 v(Location v) {
		return new Vec3(v.getX(), v.getY(), v.getZ());
	}

	public static Vec3 v(Entity entity) {
		return v(entity.getLocation());
	}

	public static Vec3 v(Block block) {
		return new Vec3(block.getX(), block.getY(), block.getZ());
	}

	public static Vec3 vCenter(Block block) {
		return new Vec3(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5);
	}

	public EulerAngle eulerAngles() {
		return new EulerAngle(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
	}

	public static Vec3 v(EulerAngle a) {
		return new Vec3(Math.toDegrees(a.getX()), Math.toDegrees(a.getY()), Math.toDegrees(a.getZ()));
	}
}
