package org.shsts.tinactory.integration.waila;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import snownee.jade.addon.harvest.ToolHandler;

import java.util.List;

import static org.shsts.tinactory.core.tool.ToolItem.HIDE_BAR;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TagToolHandler implements ToolHandler {
    private final String name;
    private final TagKey<Block> tag;
    private final IEntry<Item> item;

    public TagToolHandler(String name, TagKey<Block> tag, IEntry<Item> item) {
        this.name = name;
        this.tag = tag;
        this.item = item;
    }

    private ItemStack getItem() {
        var ret = new ItemStack(item.get());
        var tag = ret.getOrCreateTag();
        tag.putBoolean(HIDE_BAR, true);
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
    public String getName() {
        return name;
    }
}
