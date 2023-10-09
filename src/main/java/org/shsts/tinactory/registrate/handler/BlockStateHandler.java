package org.shsts.tinactory.registrate.handler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockStateHandler extends DataHandler<BlockStateProvider> {
    public BlockStateHandler(Registrate registrate) {
        super(registrate);
    }

    private static class BlockProvider extends BlockModelProvider {
        public BlockProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
            super(generator, modid, existingFileHelper);
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
        public BlockModelBuilder getBuilder(String path) {
            return super.getBuilder(modelPath(path, this.modid, this.folder));
        }

        @Override
        protected void registerModels() {}
    }

    private static class ItemProvider extends ItemModelProvider {
        public ItemProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
            super(generator, modid, existingFileHelper);
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
        protected void registerModels() {}
    }

    private class Provider extends BlockStateProvider {
        private final ItemModelProvider itemModels;
        private final BlockModelProvider blockModels;

        public Provider(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
            super(gen, modid, exFileHelper);
            this.blockModels = new BlockProvider(gen, modid, exFileHelper);
            this.itemModels = new ItemProvider(gen, modid, exFileHelper);
        }

        @Override
        public BlockModelProvider models() {
            return this.blockModels;
        }

        @Override
        public ItemModelProvider itemModels() {
            return this.itemModels;
        }

        @Override
        protected void registerStatesAndModels() {
            BlockStateHandler.this.register(this);
        }
    }

    public void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        generator.addProvider(new Provider(generator, this.registrate.modid, event.getExistingFileHelper()));
    }
}
