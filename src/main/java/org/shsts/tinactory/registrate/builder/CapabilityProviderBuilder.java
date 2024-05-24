package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.SimpleBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CapabilityProviderBuilder<T, P> extends
        SimpleBuilder<Function<T, ICapabilityProvider>, P, CapabilityProviderBuilder<T, P>> {
    public final ResourceLocation loc;

    public CapabilityProviderBuilder(P parent, ResourceLocation loc) {
        super(parent);
        this.loc = loc;
    }

    public CapabilityProviderBuilder(P parent, String id) {
        this(parent, ModelGen.modLoc(id));
    }

    public static <T extends BlockEntity, P> CapabilityProviderBuilder<T, P>
    fromFactory(P parent, ResourceLocation loc, Function<T, ICapabilityProvider> factory) {
        return new CapabilityProviderBuilder<>(parent, loc) {
            @Override
            protected Function<T, ICapabilityProvider> createObject() {
                return factory;
            }
        };
    }

    public static <T extends BlockEntity, P> CapabilityProviderBuilder<T, P>
    fromFactory(P parent, String id, Function<T, ICapabilityProvider> factory) {
        return fromFactory(parent, ModelGen.modLoc(id), factory);
    }
}
