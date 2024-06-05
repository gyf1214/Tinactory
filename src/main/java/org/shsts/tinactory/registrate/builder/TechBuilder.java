package org.shsts.tinactory.registrate.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.handler.TechProvider;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechBuilder<P> extends Builder<ResourceLocation, P, TechBuilder<P>> {
    private final List<ResourceLocation> depends = new ArrayList<>();
    private long maxProgress = 0;
    private final Map<String, Integer> modifiers = new HashMap<>();
    @Nullable
    private ResourceLocation displayItem = null;

    public TechBuilder(Registrate registrate, P parent, String id) {
        super(registrate, parent, id);
    }

    public TechBuilder<P> depends(ResourceLocation... loc) {
        depends.addAll(List.of(loc));
        return this;
    }

    public TechBuilder<P> maxProgress(long maxProgress) {
        this.maxProgress = maxProgress;
        return this;
    }

    public TechBuilder<P> displayItem(ResourceLocation loc) {
        displayItem = loc;
        return this;
    }

    public TechBuilder<P> displayItem(ItemLike item) {
        var loc = item.asItem().getRegistryName();
        assert loc != null;
        return displayItem(loc);
    }

    public TechBuilder<P> modifier(String key, int val) {
        modifiers.put(key, val);
        return this;
    }

    @Override
    protected ResourceLocation createObject() {
        assert maxProgress > 0;
        registrate.techHandler.addCallback(prov -> prov.addTech(this));
        return loc;
    }

    public JsonObject serialize() {
        var jo = new JsonObject();
        jo.addProperty("max_progress", maxProgress);
        var ja = new JsonArray();
        depends.forEach(d -> ja.add(d.toString()));
        jo.add("depends", ja);
        var displayItemLoc = displayItem == null ? Items.AIR.getRegistryName() : displayItem;
        assert displayItemLoc != null;
        jo.addProperty("display_item", displayItemLoc.toString());
        var jo1 = new JsonObject();
        for (var entry : modifiers.entrySet()) {
            jo1.addProperty(entry.getKey(), entry.getValue());
        }
        jo.add("modifiers", jo1);
        return jo;
    }

    public void validate(ExistingFileHelper existingFileHelper) {
        for (var loc : depends) {
            if (!existingFileHelper.exists(loc, TechProvider.RESOURCE_TYPE)) {
                throw new IllegalStateException("Technology at %s does not exist".formatted(loc));
            }
        }
    }
}
