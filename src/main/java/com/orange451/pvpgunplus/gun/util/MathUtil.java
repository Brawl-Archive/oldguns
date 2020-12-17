//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.orange451.pvpgunplus.gun.util;

import java.util.*;

public final class MathUtil {
	public static final double EXPANDER = Math.pow(2, 24);
	public static final double PHI = 1.618033988749895D;
	public static final double SQRT_2 = Math.sqrt(2.0D);
	public static final float SQRT_2_F = sqrtf(2.0F);
	public static final float PI_F = 3.1415927F;
	public static final long ZERO_DOUBLE_BITS = Double.doubleToRawLongBits(0.0D);
	public static final int ZERO_FLOAT_BITS = Float.floatToRawIntBits(0.0F);
	private static final double F_3_4 = 0.75D;
	private static final double F_1_2 = 0.5D;
	private static final double F_1_4 = 0.25D;
	private static final double[] TANGENT_TABLE_A = new double[] { 0.0D, 0.1256551444530487D, 0.25534194707870483D, 0.3936265707015991D, 0.5463024377822876D, 0.7214844226837158D, 0.9315965175628662D, 1.1974215507507324D, 1.5574076175689697D, 2.092571258544922D, 3.0095696449279785D, 5.041914939880371D, 14.101419448852539D, -18.430862426757812D };
	private static final double[] TANGENT_TABLE_B = new double[] { 0.0D, -7.877917738262007E-9D, -2.5857668567479893E-8D, 5.2240336371356666E-9D, 5.206150291559893E-8D, 1.8307188599677033E-8D, -5.7618793749770706E-8D, 7.848361555046424E-8D, 1.0708593250394448E-7D, 1.7827257129423813E-8D, 2.893485277253286E-8D, 3.1660099222737955E-7D, 4.983191803254889E-7D, -3.356118100840571E-7D };
	private static final double[] EIGHTHS = new double[] { 0.0D, 0.125D, 0.25D, 0.375D, 0.5D, 0.625D, 0.75D, 0.875D, 1.0D, 1.125D, 1.25D, 1.375D, 1.5D, 1.625D };
	private static final long HEX_40000000 = 1073741824L;
	private static final long MASK_30BITS = -1073741824L;
	public static final double EPSILON = Double.longBitsToDouble(4368491638549381120L);
	public static final double SAFE_MIN = Double.longBitsToDouble(4503599627370496L);
	private static final long EXPONENT_OFFSET = 1023L;

	private MathUtil() {
	}

	public static int log2(int bits) {
		return bits <= 0 ? 0 : 31 - Integer.numberOfLeadingZeros(bits);
	}

	public static double pow_est(double a, double b) {
		long tmp = Double.doubleToLongBits(a);
		long tmp2 = (long) (b * (double) (tmp - 4606921280493453312L)) + 4606921280493453312L;
		return Double.longBitsToDouble(tmp2);
	}

	public static float powf_est(float a, float b) {
		int tmp = Float.floatToIntBits(a);
		int tmp2 = (int) (b * (float) (tmp - 1064243839)) + 1064243839;
		return Float.intBitsToFloat(tmp2);
	}

	public static int clamp(int x, int min, int max) {
		if (x <= min) {
			return min;
		} else {
			return x >= max ? max : x;
		}
	}

	public static float toRadiansf(float angdeg) {
		return angdeg / 180.0F * 3.1415927F;
	}

	public static float toDegreesf(float angrad) {
		return angrad * 180.0F / 3.1415927F;
	}

	public static float atan2f(float a, float b) {
		return (float) Math.atan2((double) a, (double) b);
	}

	public static float atanf(float a) {
		return (float) Math.atan((double) a);
	}

	public static float tanf(float a) {
		return (float) Math.tan((double) a);
	}

	public static float sinf(float a) {
		return (float) Math.sin((double) a);
	}

	public static float cosf(float a) {
		return (float) Math.cos((double) a);
	}

	public static float acosf(float a) {
		return (float) Math.acos((double) a);
	}

	public static float powf(float a, float b) {
		return (float) Math.pow((double) a, (double) b);
	}

	public static float floorf(float f) {
		return (float) Math.floor((double) f);
	}

	public static float ceilf(float f) {
		return (float) Math.ceil((double) f);
	}

	public static double sqrt_fast(double d) {
		double sqrt = Double.longBitsToDouble((Double.doubleToLongBits(d) - 4503599627370496L >> 1) + 2305843009213693952L);
		sqrt = (sqrt + d / sqrt) / 2.0D;
		sqrt = (sqrt + d / sqrt) / 2.0D;
		return sqrt;
	}

	public static float sqrtf_fast(float d) {
		float sqrt = Float.intBitsToFloat((Float.floatToIntBits(d) - 8388608 >> 1) + 536870912);
		sqrt = (sqrt + d / sqrt) / 2.0F;
		sqrt = (sqrt + d / sqrt) / 2.0F;
		return sqrt;
	}

	public static float sqrtf(float f) {
		return sqrtf_fast(f);
	}

	public static int bitsf(float v, float epsilon) {
		if (epsilon != 0.0F) {
			v -= v % epsilon;
		}

		return Float.floatToRawIntBits(v);
	}

	public static float angleDiff(float a, float b) {
		float diff = b - a;
		if (diff > 180.0F) {
			diff -= 360.0F;
		} else if (diff < -180.0F) {
			diff += 360.0F;
		}

		return diff;
	}

	public static double angleDiff(double a, double b) {
		double diff = b - a;
		if (diff > 180.0D) {
			diff -= 360.0D;
		} else if (diff < -180.0D) {
			diff += 360.0D;
		}

		return diff;
	}

	public static float normalizeAngle(float aa) {
		double a = (double) aa;
		if (a < 0.0D) {
			a += (Math.floor(-a / 360.0D) + 1.0D) * 360.0D;
		}

		return (float) (a % 360.0D);
	}

	public static double normalizeAngle(double aa) {
		double a = aa;
		if (aa < 0.0D) {
			a = aa + (Math.floor(-aa / 360.0D) + 1.0D) * 360.0D;
		}

		return a % 360.0D;
	}

	public static int safelyMultiply(int a, int b) {
		return clip32((long) a * (long) b);
	}

	public static long safelyMultiply(long a, long b) {
		if (a == 0L) {
			return 0L;
		} else {
			long c = a * b;
			if (c / a == b) {
				return c;
			} else {
				return (a >>> 63 ^ b >>> 63) == 0L ? 9223372036854775807L : -9223372036854775808L;
			}
		}
	}

	public static int clip32(long v) {
		if (v < -2147483648L) {
			return -2147483648;
		} else {
			return v > 2147483647L ? 2147483647 : (int) v;
		}
	}

	public static long bits(double v, double epsilon) {
		if (epsilon != 0.0D) {
			v -= v % epsilon;
		}

		return Double.doubleToRawLongBits(v);
	}

	public static boolean compareDoubles(double a, double b) {
		return isZero((float) Double.doubleToRawLongBits(b - a));
	}

	public static boolean compareFloats(float a, float b) {
		return isZero((float) Float.floatToRawIntBits(b - a));
	}

	public static strictfp boolean isZero(double value) {
		return value == 0.0D;
	}

	public static strictfp boolean isZero(float value) {
		return (double) value == 0.0D;
	}

	public static double fixDouble(double value) {
		return Double.longBitsToDouble(Double.doubleToRawLongBits(value));
	}

	public static float fixFloat(float value) {
		return Float.intBitsToFloat(Float.floatToRawIntBits(value));
	}

	public static int safelySum(int a, int b) {
		if (a > b) {
			int tmp = a;
			a = b;
			b = tmp;
		}

		if (a < 0) {
			if (b >= 0) {
				return a + b;
			} else {
				return -2147483648 - b <= a ? a + b : -2147483648;
			}
		} else {
			return a <= 2147483647 - b ? a + b : 2147483647;
		}
	}

	public static long safelySum(long a, long b) {
		if (a > b) {
			long tmp = a;
			a = b;
			b = tmp;
		}

		if (a < 0L) {
			if (b >= 0L) {
				return a + b;
			} else {
				return -9223372036854775808L - b <= a ? a + b : -9223372036854775808L;
			}
		} else {
			return a <= 9223372036854775807L - b ? a + b : 9223372036854775807L;
		}
	}

	public static double squareSafe(double value) {
		return isZero(value) ? 0.0D : value * value;
	}

	public static double lengthSafe(double dx, double dy, double dz) {
		return squareSafe(dx) + squareSafe(dy) + squareSafe(dz);
	}

	public static double length(double dx, double dy, double dz) {
		return dx * dx + dy * dy + dz * dz;
	}

	public static int minimum(int a, int b, int c) {
		return a < b ? (a < c ? a : c) : (b < c ? b : c);
	}

	public static int clip(int val, int min, int max) {
		if (val < min) {
			val = min;
		}

		if (val > max) {
			val = max;
		}

		return val;
	}

	public static int floor(float val) {
		int n = (int) val;
		return val < (float) n ? n - 1 : n;
	}

	public static int floor(double val) {
		int n = (int) val;
		return val < (double) n ? n - 1 : n;
	}

	public static int ceil(float val) {
		int n = (int) val;
		return val > (float) n ? n + 1 : n;
	}

	public static int ceil(double val) {
		int n = (int) val;
		return val > (double) n ? n + 1 : n;
	}

	public static double min(double v1, double v2, double v3, double v4) {
		if (v1 > v2) {
			v1 = v2;
		}

		if (v1 > v3) {
			v1 = v3;
		}

		if (v1 > v4) {
			v1 = v4;
		}

		return v1;
	}

	public static double max(double v1, double v2, double v3, double v4) {
		if (v1 < v2) {
			v1 = v2;
		}

		if (v1 < v3) {
			v1 = v3;
		}

		if (v1 < v4) {
			v1 = v4;
		}

		return v1;
	}

	public static int random(int start, int end) {
		if (end > start) {
			int tmp = start;
			start = end;
			end = tmp;
		}

		return start + (int) (Math.random() * (double) (end - start + 1));
	}

	public static long gcd(long a, long b) {
		if (a < 0L) {
			a = -a;
		}

		if (b == 0L) {
			return a;
		} else {
			if (b < 0L) {
				b = -b;
			}

			if (a == 0L) {
				return b;
			} else {
				long t;
				for (t = 0L; ((a | b) & 1L) == 0L; ++t) {
					a >>>= 1;
					b >>>= 1;
				}

				while ((a & 1L) == 0L) {
					a >>>= 1;
				}

				while ((b & 1L) == 0L) {
					b >>>= 1;
				}

				while (true) {
					while (a != b) {
						if (a > b) {
							a -= b;

							while (true) {
								a >>>= 1;
								if ((a & 1L) != 0L) {
									break;
								}
							}
						} else {
							b -= a;

							while (true) {
								b >>>= 1;
								if ((b & 1L) != 0L) {
									break;
								}
							}
						}
					}

					return a << (int) t;
				}
			}
		}
	}

	public static int mode(Collection<Integer> c) {
		int maxValue = 0, maxCount = 0;

		for (int i : c) {
			int count = 0;
			for (int j : c) {
				if (i == j) {
					count++;
				}
			}

			if (count > maxCount) {
				maxCount = count;
				maxValue = i;
			}
		}

		return maxValue;
	}

	public static int gcd(int a, int b) {
		if (a < 0) {
			a = -a;
		}

		if (b == 0) {
			return a;
		} else {
			if (b < 0) {
				b = -b;
			}

			if (a == 0) {
				return b;
			} else {
				int t;
				for (t = 0; ((a | b) & 1) == 0; ++t) {
					a >>>= 1;
					b >>>= 1;
				}

				while ((a & 1) == 0) {
					a >>>= 1;
				}

				while ((b & 1) == 0) {
					b >>>= 1;
				}

				while (true) {
					while (a != b) {
						if (a > b) {
							a -= b;

							while (true) {
								a >>>= 1;
								if ((a & 1) != 0) {
									break;
								}
							}
						} else {
							b -= a;

							while (true) {
								b >>>= 1;
								if ((b & 1) != 0) {
									break;
								}
							}
						}
					}

					return a << t;
				}
			}
		}
	}

	public static boolean isPrime(long n) {
		if (n < 2L) {
			return false;
		} else if (n != 2L && n != 3L) {
			if ((n & 1L) != 0L && n % 3L != 0L) {
				long max = (long) Math.sqrt((double) n) + 1L;

				for (long i = 6L; i <= max; i += 6L) {
					if (n % (i - 1L) == 0L || n % (i + 1L) == 0L) {
						return false;
					}
				}

				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public static int swapBits(int v) {
		return (v & 255) << 24 | (v >>> 8 & 255) << 16 | (v >>> 16 & 255) << 8 | v >>> 24;
	}

	public static long modPow(long a, long b, long mod) {
		long product = 1L;

		for (long pseq = a % mod; b > 0L; b >>>= 1) {
			if ((b & 1L) != 0L) {
				product = modMult(product, pseq, mod);
			}

			pseq = modMult(pseq, pseq, mod);
		}

		return product;
	}

	public static long modMult(long a, long b, long mod) {
		if (a != 0L && b >= mod / a) {
			long sum;
			for (sum = 0L; b > 0L; b >>>= 1) {
				if ((b & 1L) != 0L) {
					sum = (sum + a) % mod;
				}

				a = (a << 1) % mod;
			}

			return sum;
		} else {
			return a * b % mod;
		}
	}

	public static long modInverse(long a, long n) {
		long i = n;
		long v = 0L;

		long x;
		for (long d = 1L; a > 0L; v = x) {
			long t = i / a;
			x = a;
			a = i % a;
			i = x;
			x = d;
			d = v - t * d;
		}

		v %= n;
		if (v < 0L) {
			v = (v + n) % n;
		}

		return v;
	}

	public static double copySign(double magnitude, double sign) {
		long m = Double.doubleToRawLongBits(magnitude);
		long s = Double.doubleToRawLongBits(sign);
		return (m ^ s) >= 0L ? magnitude : -magnitude;
	}

	private static double doubleHighPart(double d) {
		if (d > -SAFE_MIN && d < SAFE_MIN) {
			return d;
		} else {
			long xl = Double.doubleToRawLongBits(d);
			xl &= -1073741824L;
			return Double.longBitsToDouble(xl);
		}
	}

	public static double atan(double x) {
		return atan(x, 0.0D, false);
	}

	private static double atan(double xa, double xb, boolean leftPlane) {
		if (xa == 0.0D) {
			return leftPlane ? copySign(3.141592653589793D, xa) : xa;
		} else {
			boolean negate;
			if (xa < 0.0D) {
				xa = -xa;
				xb = -xb;
				negate = true;
			} else {
				negate = false;
			}

			if (xa > 1.633123935319537E16D) {
				return negate ^ leftPlane ? -1.5707963267948966D : 1.5707963267948966D;
			} else {
				int idx;
				double ttA;
				if (xa < 1.0D) {
					idx = (int) ((-1.7168146928204135D * xa * xa + 8.0D) * xa + 0.5D);
				} else {
					ttA = 1.0D / xa;
					idx = (int) (-((-1.7168146928204135D * ttA * ttA + 8.0D) * ttA) + 13.07D);
				}

				ttA = TANGENT_TABLE_A[idx];
				double ttB = TANGENT_TABLE_B[idx];
				double epsA = xa - ttA;
				double epsB = -(epsA - xa + ttA);
				epsB += xb - ttB;
				double temp = epsA + epsB;
				epsB = -(temp - epsA - epsB);
				epsA = temp;
				temp = xa * 1.073741824E9D;
				double ya = xa + temp - temp;
				double yb = xb + xa - ya;
				xb += yb;
				double pia;
				double epsA2;
				double eighths;
				double za;
				double zb;
				double result;
				double resultb;
				if (idx == 0) {
					epsA2 = 1.0D / (1.0D + (ya + xb) * (ttA + ttB));
					ya = epsA * epsA2;
					yb = epsB * epsA2;
				} else {
					epsA2 = ya * ttA;
					eighths = 1.0D + epsA2;
					za = -(eighths - 1.0D - epsA2);
					epsA2 = xb * ttA + ya * ttB;
					temp = eighths + epsA2;
					za += -(temp - eighths - epsA2);
					eighths = temp;
					za += xb * ttB;
					ya = epsA / temp;
					temp = ya * 1.073741824E9D;
					zb = ya + temp - temp;
					result = ya - zb;
					temp = eighths * 1.073741824E9D;
					resultb = eighths + temp - temp;
					pia = eighths - resultb;
					yb = (epsA - zb * resultb - zb * pia - result * resultb - result * pia) / eighths;
					yb += -epsA * za / eighths / eighths;
					yb += epsB / eighths;
				}

				epsB = yb;
				epsA2 = ya * ya;
				yb = 0.07490822288864472D;
				yb = yb * epsA2 - 0.09088450866185192D;
				yb = yb * epsA2 + 0.11111095942313305D;
				yb = yb * epsA2 - 0.1428571423679182D;
				yb = yb * epsA2 + 0.19999999999923582D;
				yb = yb * epsA2 - 0.33333333333333287D;
				yb = yb * epsA2 * ya;
				temp = ya + yb;
				yb = -(temp - ya - yb);
				yb += epsB / (1.0D + ya * ya);
				eighths = EIGHTHS[idx];
				za = eighths + temp;
				zb = -(za - eighths - temp);
				temp = za + yb;
				zb += -(temp - za - yb);
				result = temp + zb;
				if (leftPlane) {
					resultb = -(result - temp - zb);
					pia = 3.141592653589793D;
					double pib = 1.2246467991473532E-16D;
					za = 3.141592653589793D - result;
					zb = -(za - 3.141592653589793D + result);
					zb += 1.2246467991473532E-16D - resultb;
					result = za + zb;
				}

				if (negate ^ leftPlane) {
					result = -result;
				}

				return result;
			}
		}
	}

	public static double atan2(double y, double x) {
		if (x == x && y == y) {
			double r;
			double ra;
			double rb;
			if (y == 0.0D) {
				r = x * y;
				ra = 1.0D / x;
				rb = 1.0D / y;
				if (ra == 0.0D) {
					return x > 0.0D ? y : copySign(3.141592653589793D, y);
				} else if (x >= 0.0D && ra >= 0.0D) {
					return r;
				} else {
					return y >= 0.0D && rb >= 0.0D ? 3.141592653589793D : -3.141592653589793D;
				}
			} else if (y == 1.0D / 0.0) {
				if (x == 1.0D / 0.0) {
					return 0.7853981633974483D;
				} else {
					return x == -1.0D / 0.0 ? 2.356194490192345D : 1.5707963267948966D;
				}
			} else if (y == -1.0D / 0.0) {
				if (x == 1.0D / 0.0) {
					return -0.7853981633974483D;
				} else {
					return x == -1.0D / 0.0 ? -2.356194490192345D : -1.5707963267948966D;
				}
			} else {
				if (x == 1.0D / 0.0) {
					if (y > 0.0D || 1.0D / y > 0.0D) {
						return 0.0D;
					}

					if (y < 0.0D || 1.0D / y < 0.0D) {
						return -0.0D;
					}
				}

				if (x == -1.0D / 0.0) {
					if (y > 0.0D || 1.0D / y > 0.0D) {
						return 3.141592653589793D;
					}

					if (y < 0.0D || 1.0D / y < 0.0D) {
						return -3.141592653589793D;
					}
				}

				if (x == 0.0D) {
					if (y > 0.0D || 1.0D / y > 0.0D) {
						return 1.5707963267948966D;
					}

					if (y < 0.0D || 1.0D / y < 0.0D) {
						return -1.5707963267948966D;
					}
				}

				r = y / x;
				if (Double.isInfinite(r)) {
					return atan(r, 0.0D, x < 0.0D);
				} else {
					ra = doubleHighPart(r);
					rb = r - ra;
					double xa = doubleHighPart(x);
					double xb = x - xa;
					rb += (y - ra * xa - ra * xb - rb * xa - rb * xb) / x;
					double temp = ra + rb;
					rb = -(temp - ra - rb);
					ra = temp;
					if (temp == 0.0D) {
						ra = copySign(0.0D, y);
					}

					double result = atan(ra, rb, x < 0.0D);
					return result;
				}
			}
		} else {
			return 0.0D / 0.0;
		}
	}
}
