package org.shsts.tinactory.compat.waila;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import snownee.jade.addon.harvest.ToolHandler;

import java.util.List;

import static org.shsts.tinactory.AllDataComponents.HIDE_BAR;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TagToolHandler implements ToolHandler {
    private final ResourceLocation uid;
    private final TagKey<Block> tag;
    private final IEntry<Item> item;

    public TagToolHandler(String name, TagKey<Block> tag, IEntry<Item> item) {
        this.uid = modLoc("harvest_tool/" + name);
        this.tag = tag;
        this.item = item;
    }

    private ItemStack getItem() {
        var ret = new ItemStack(item.get());
        ret.set(HIDE_BAR, true);
        return ret;
    }

    @Override
    public ItemStack test(BlockState blockState, Level level, BlockPos blockPos) {
        if (!blockState.is(tag)) {
            return ItemStack.EMPTY;
        }
        return getItem();
    }

    @Override
    public List<ItemStack> getTools() {
        return List.of(getItem());
    }

    @Override
    public ResourceLocation getUid() {
        return uid;
    }
}
