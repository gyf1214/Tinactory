package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.electric.Voltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PoweredWeaponConfig(Voltage voltage, long capacity, long hitCost,
    double attackDamage, double attackSpeed) {
    public PoweredWeaponConfig {
        if (capacity <= 0L || hitCost <= 0L || attackDamage <= 0.0d || attackSpeed <= 0.0d) {
            throw new IllegalArgumentException("Powered weapon values must be positive");
        }
        if (hitCost > capacity) {
            throw new IllegalArgumentException("Powered weapon hit cost cannot exceed its capacity");
        }
    }
}
