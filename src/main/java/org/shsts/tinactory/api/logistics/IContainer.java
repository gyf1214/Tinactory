package org.shsts.tinactory.api.logistics;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.tech.ITeamProfile;

import java.util.Optional;
import java.util.function.Predicate;

public interface IContainer {
    Optional<? extends ITeamProfile> getOwnerTeam();

    int portSize();

    boolean hasPort(int port);

    PortDirection portDirection(int port);

    IPort getPort(int port, boolean internal);

    void setItemFilter(int port, Predicate<ItemStack> filter);

    void setFluidFilter(int port, Predicate<FluidStack> filter);

    void resetFilter(int port);
}
