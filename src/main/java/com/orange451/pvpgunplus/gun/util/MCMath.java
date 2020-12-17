package com.orange451.pvpgunplus.gun.util;

import org.bukkit.util.*;

/**
 * @author Joseph Robert Melsha (joe.melsha@live.com)
 * @link http://www.joemelsha.com
 * @date Jan 21, 2015
 *
 * Copyright 2015 Poofless Gaming, LLC
 */
public class MCMath {
	private static final int intmax = Integer.MAX_VALUE;

	public static int blockInt(double v) {
		return floorInt(v);
	}

	public static int floorInt(float v) {
		if (Float.isNaN(v))
			return 0;
		if (v < -intmax)
			return -intmax;
		if (v > intmax)
			return intmax;
		return NumberConversions.floor(v);
	}

	public static int ceilInt(float v) {
		if (Float.isNaN(v))
			return 0;
		if (v < -intmax)
			return -intmax;
		if (v > intmax)
			return intmax;
		return NumberConversions.ceil(v);
	}

	public static int floorInt(double v) {
		if (Double.isNaN(v))
			return 0;
		if (v < -intmax)
			return -intmax;
		if (v > intmax)
			return intmax;
		return NumberConversions.floor(v);
	}

	public static int ceilInt(double v) {
		if (Double.isNaN(v))
			return 0;
		if (v < -intmax)
			return -intmax;
		if (v > intmax)
			return intmax;
		return NumberConversions.ceil(v);
	}

	public static int roundInt(float v) {
		return floorInt(v + 0.5F);
	}

	public static int roundInt(double v) {
		return floorInt(v + 0.5D);
	}

	//returns the distance from P to any point on line segment P0->P1
	public static double pointToLine(Vec3 P, Vec3 P0, Vec3 P1) {
		Vec3 v = P1.sub(P0), w = P.sub(P0);
		double c1 = w.dot(v);
		if (c1 <= 0)
			return P.distSq(P0);
		double c2 = v.dot(v);
		if (c2 <= c1)
			return P.distSq(P1);
		double b = c1 / c2;
		Vec3 Pb = P0.add(v.mul(b));
		return P.distSq(Pb);
	}

	public static int dirToInt(float dir) {
		return roundInt((normalizeAngle(dir) * 256F / 360F)) & 0xff;
	}

	public static float intToDir(int dir) {
		return dir * 360F / 256F;
	}

	public static IntVector3 limit(double x, double y, double z) {
		x *= 8000;
		y *= 8000;
		z *= 8000;
		double ax = Math.abs(x), ay = Math.abs(y), az = Math.abs(z);

		double max = ax;
		if (max < ay) max = ay;
		if (max < az) max = az;
		if (Short.MAX_VALUE < max) {
			double r = Short.MAX_VALUE / max;
			x *= r;
			y *= r;
			z *= r;
		}

		int ix = roundInt(x);
		if (ix < Short.MIN_VALUE) ix = Short.MIN_VALUE;
		else if (ix > Short.MAX_VALUE) ix = Short.MAX_VALUE;

		int iy = roundInt(y);
		if (iy < Short.MIN_VALUE) iy = Short.MIN_VALUE;
		else if (iy > Short.MAX_VALUE) iy = Short.MAX_VALUE;

		int iz = roundInt(z);
		if (iz < Short.MIN_VALUE) iz = Short.MIN_VALUE;
		else if (iz > Short.MAX_VALUE) iz = Short.MAX_VALUE;

		return new IntVector3(ix, iy, iz);
	}

	public static double VELOCITY_LIMIT = Short.MAX_VALUE / 8000.0D;

	public static Vector limit(Vector v) {
		IntVector3 iv = limit(v.getX(), v.getY(), v.getZ());
		v.setX(iv.getX() / 8000D);
		v.setY(iv.getY() / 8000D);
		v.setZ(iv.getZ() / 8000D);
		return v;
	}

	/**
	 * Arguments must be from 0-360
	 */
	public static float angleDiff(float a, float b) {
		float diff = b - a;
		if (diff > 180)
			diff -= 360;
		else if (diff < -180)
			diff += 360;
		return diff;
	}

	/**
	 * Arguments must be from 0-360
	 */
	public static double angleDiffD(double a, double b) {
		double diff = b - a;
		if (diff > 180)
			diff -= 360;
		else if (diff < -180)
			diff += 360;
		return diff;
	}

	public static float normalizeAngle(float aa) {
		double a = aa;
		if (a < 0)
			a += (Math.floor(-a / 360) + 1) * 360;
		return (float) (a % 360);
	}

	public static double normalizeAngleD(double aa) {
		double a = aa;
		if (a < 0)
			a += (Math.floor(-a / 360) + 1) * 360;
		return a % 360;
	}
}
