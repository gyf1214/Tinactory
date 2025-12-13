package org.shsts.tinactory.integration.waila;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.core.logistics.StackHelper;
import snownee.jade.Jade;
import snownee.jade.JadeCommonConfig;
import snownee.jade.VanillaPlugin;

import java.util.ArrayList;

import static org.shsts.tinactory.AllCapabilities.ITEM_HANDLER;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ContainerProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {
    public static final ContainerProvider INSTANCE = new ContainerProvider();
    public static final ResourceLocation ITEMS = modLoc("items");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var tag = accessor.getServerData();
        if (tag.contains("tinactoryItems", Tag.TAG_LIST)) {
            // remove Jade tooltip first
            tooltip.remove(VanillaPlugin.INVENTORY);

            // use the same logic as Jade
            var listTag = tag.getList("tinactoryItems", Tag.TAG_COMPOUND);
            var elements = new ArrayList<IElement>();
            var showName = listTag.size() < 5;
            var helper = tooltip.getElementHelper();
            var drawnCount = 0;
            var first = true;

            for (var itemTag : listTag) {
                if (!first && (showName || drawnCount >= JadeCommonConfig.inventoryShowItemPreLine)) {
                    tooltip.add(elements);
                    elements.clear();
                    drawnCount = 0;
                }

                var stack = StackHelper.deserializeItemStack((CompoundTag) itemTag);
                if (showName) {
                    var stack1 = stack.copy();
                    stack1.setCount(1);
                    elements.add(Jade.smallItem(helper, stack1).tag(ITEMS));
                    var text = new TextComponent(NUMBER_FORMAT.format(stack.getCount()))
                        .append("× ").append(stack.getHoverName());
                    elements.add(helper.text(text).tag(ITEMS).message(null));
                } else {
                    elements.add(helper.item(stack).tag(ITEMS));
                }

                ++drawnCount;
                first = false;
            }

            if (!elements.isEmpty()) {
                tooltip.add(elements);
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level world,
        BlockEntity blockEntity, boolean showDetails) {
        if (JadeCommonConfig.shouldIgnoreTE(blockEntity.getType())) {
            return;
        }

        var handler = ITEM_HANDLER.tryGet(blockEntity);
        if (handler.isEmpty()) {
            return;
        }
        var limit = showDetails ? JadeCommonConfig.inventoryDetailedShowAmount :
            JadeCommonConfig.inventoryNormalShowAmount;

        var listTag = new ListTag();
        var items = handler.get();
        for (var i = 0; i < items.getSlots(); i++) {
            if (limit <= 0) {
                break;
            }
            var stack = items.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            listTag.add(StackHelper.serializeItemStack(stack));
            limit--;
        }

        if (!listTag.isEmpty()) {
            tag.put("tinactoryItems", listTag);
        }
    }
}
