package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechBuilder<P> extends Builder<ResourceLocation, P, TechBuilder<P>> {
    private final List<ResourceLocation> depends = new ArrayList<>();
    private long maxProgress = 0;

    public TechBuilder(Registrate registrate, P parent, String id) {
        super(registrate, parent, id);
    }

    public TechBuilder<P> maxProgress(long maxProgress) {
        this.maxProgress = maxProgress;
        return this;
    }

    public TechBuilder<P> depends(ResourceLocation... loc) {
        depends.addAll(List.of(loc));
        return this;
    }

    @Override
    protected ResourceLocation createObject() {
        assert maxProgress > 0;
        registrate.techHandler.addCallback(prov -> prov.addTech(loc, depends, maxProgress));
        return loc;
    }
}
