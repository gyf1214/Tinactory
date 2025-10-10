package org.shsts.tinactory.core.util;

import com.mojang.math.MethodsReturnNonnullByDefault;
import com.mojang.math.Vector3f;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class MathUtil {
    public static final double EPS = 1e-6d;

    public static int compare(double sth) {
        return compare(sth, EPS);
    }

    public static int compare(double sth, double threshold) {
        return Math.abs(sth) <= threshold ? 0 : (sth > threshold ? 1 : -1);
    }

    public static double clamp(double x, double min, double max) {
        return Math.max(Math.min(x, max), min);
    }

    public static int clamp(int x, int min, int max) {
        return Math.max(Math.min(x, max), min);
    }

    public static long clamp(long x, long min, long max) {
        return Math.max(Math.min(x, max), min);
    }

    public static Vector3f mulVecf(Vector3f x, float k) {
        x = x.copy();
        x.mul(k);
        return x;
    }

    public static Vector3f addVecf(Vector3f x, Vector3f... other) {
        x = x.copy();
        for (var vec : other) {
            x.add(vec);
        }
        return x;
    }

    public static Vec3 blockCenter(BlockPos pos) {
        return (new Vec3(pos.getX(), pos.getY(), pos.getZ()))
            .add(0.5, 0.5, 0.5);
    }

    public static Vec3 dirNormal(Direction dir) {
        return new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
    }

    public static double safePow(double a, double b) {
        return a > EPS ? Math.pow(a, b) : 0d;
    }

    private static int directBinomial(int n, double p, Random random) {
        var ret = 0;
        for (var i = 0; i < n; i++) {
            if (random.nextDouble() <= p) {
                ret++;
            }
        }
        return ret;
    }

    /**
     * Sample from Poisson(lambda)
     */
    private static int samplePoisson(double lambda, Random random) {
        var l = Math.exp(-lambda);
        var p = random.nextDouble();
        var k = 0;
        while (p > l) {
            p = p * random.nextDouble();
            k++;
        }
        return k;
    }

    /**
     * Sample from Binomial(n, p).
     */
    public static int sampleBinomial(int n, double p, Random random) {
        // if n is small, sample directly
        if (n < 32) {
            return directBinomial(n, p, random);
        }

        // if p or 1 - p is small, sample from poisson
        if (p < 0.05) {
            var l = n * p;
            if (l < 10) {
                return Math.min(n, samplePoisson(l, random));
            }
        } else if (p > 0.95) {
            var l = n * (1 - p);
            if (l < 10) {
                return n - Math.min(n, samplePoisson(l, random));
            }
        }

        // if n * p and n * (1 - p) are both big enough, sample from normal
        if (n * p >= 10 && n * (1 - p) >= 10) {
            var mean = n * p;
            var std = Math.sqrt(n * p * (1 - p));
            var ret = clamp(Math.round(random.nextGaussian(mean, std)), 0, n);
            return (int) ret;
        }

        // nothing we can do to get a good approximation, just sample directly
        return directBinomial(n, p, random);
    }
}
