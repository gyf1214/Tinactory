package org.shsts.tinactory.api.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IComponentType<T extends INetworkComponent> {
    Class<T> clazz();

    T create(INetwork network);
}
