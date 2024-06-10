package org.shsts.tinactory.datagen.handler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.datagen.DataGen;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechHandler extends DataHandler<TechProvider> {
    public TechHandler(DataGen dataGen) {
        super(dataGen);
    }

    private class Provider extends TechProvider {
        protected Provider(GatherDataEvent event) {
            super(event.getGenerator(), dataGen.modid, event.getExistingFileHelper());
        }

        @Override
        protected void addTechs() {
            TechHandler.this.register(this);
        }
    }

    @Override
    protected TechProvider createProvider(GatherDataEvent event) {
        return new Provider(event);
    }
}
