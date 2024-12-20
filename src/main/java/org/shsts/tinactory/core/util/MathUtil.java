package org.shsts.tinactory.core.util;

import com.mojang.math.MethodsReturnNonnullByDefault;
import com.mojang.math.Vector3f;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

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
}
