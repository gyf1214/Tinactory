package org.shsts.tinactory.registrate.handler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechHandler extends DataHandler<TechProvider> {
    public TechHandler(Registrate registrate) {
        super(registrate);
    }

    private class Provider extends TechProvider {
        protected Provider(GatherDataEvent event) {
            super(event.getGenerator(), registrate.modid, event.getExistingFileHelper());
        }

        @Override
        protected void addTechs() {
            TechHandler.this.register(this);
        }
    }

    @Override
    public void onGatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(new Provider(event));
    }
}
