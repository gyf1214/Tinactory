package org.shsts.tinactory.registrate.handler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TagsHandler<T> extends DataHandler<TagsProvider<T>> {
    protected final Registry<T> registry;
    protected final ResourceKey<? extends Registry<T>> registryKey;

    public TagsHandler(Registrate registrate, Registry<T> registry) {
        super(registrate);
        this.registry = registry;
        this.registryKey = registry.key();
    }

    private class Provider extends TagsProvider<T> {
        public Provider(GatherDataEvent event) {
            super(event.getGenerator(), TagsHandler.this.registry,
                    registrate.modid, event.getExistingFileHelper());
        }

        public void addTag(TagKey<T> key, T object) {
            this.tag(key).add(object);
        }

        public void addTag(TagKey<T> key, TagKey<T> object) {
            this.tag(key).addTag(object);
        }

        @Override
        protected void addTags() {
            TagsHandler.this.register(this);
        }

        @Override
        public String getName() {
            return this.modId + " item tags";
        }
    }

    public void addTags(Supplier<? extends T> object, ResourceLocation... tags) {
        for (var tag : tags) {
            this.callbacks.add(prov -> ((Provider) prov)
                    .addTag(TagKey.create(this.registryKey, tag), object.get()));
        }
    }

    @SafeVarargs
    public final void addTags(Supplier<? extends T> object, TagKey<T>... tags) {
        for (var tag : tags) {
            this.callbacks.add(prov -> ((Provider) prov).addTag(tag, object.get()));
        }
    }

    public void addTag(TagKey<T> object, TagKey<T> tag) {
        this.callbacks.add(prov -> ((Provider) prov).addTag(tag, object));
    }

    @Override
    public void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        generator.addProvider(new Provider(event));
    }
}
