package org.shsts.tinactory.registrate.handler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemModelHandler extends DataHandler<ItemModelProvider> {
    public ItemModelHandler(Registrate registrate) {
        super(registrate);
    }

    private class Provider extends ItemModelProvider {
        public Provider(GatherDataEvent event) {
            super(event.getGenerator(), registrate.modid, event.getExistingFileHelper());
        }

        /**
         * Convention: all withExistingParent should use full path.
         */
        @Override
        public ModelFile.ExistingModelFile getExistingFile(ResourceLocation path) {
            var ret = new ModelFile.ExistingModelFile(path, existingFileHelper);
            ret.assertExistence();
            return ret;
        }

        /**
         * Convention: all getBuilder should automatically include the folder prefix.
         */
        @Override
        public ItemModelBuilder getBuilder(String path) {
            return super.getBuilder(modelPath(path, this.modid, this.folder));
        }

        @Override
        protected void registerModels() {
            ItemModelHandler.this.register(this);
        }
    }

    @Override
    public void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        generator.addProvider(new Provider(event));
    }
}
