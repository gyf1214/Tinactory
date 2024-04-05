package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.BuilderBase;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CapabilityProviderBuilder<T, P> extends
        BuilderBase<Function<T, ICapabilityProvider>, P, CapabilityProviderBuilder<T, P>> {
    public final ResourceLocation loc;

    public CapabilityProviderBuilder(P parent, ResourceLocation loc) {
        super(parent);
        this.loc = loc;
        onBuild.add(CapabilityProviderBuilder::buildObject);
    }

    public CapabilityProviderBuilder(P parent, String id) {
        this(parent, ModelGen.modLoc(id));
    }
}
