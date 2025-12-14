package org.shsts.tinactory.integration.waila;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.util.ClientUtil;
import snownee.jade.Jade;
import snownee.jade.JadeCommonConfig;
import snownee.jade.VanillaPlugin;

import java.util.ArrayList;

import static org.shsts.tinactory.AllCapabilities.BYTES_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.ITEM_HANDLER;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.integration.waila.Waila.BYTES;
import static org.shsts.tinactory.integration.waila.Waila.ENHANCE_ITEMS;
import static org.shsts.tinactory.integration.waila.Waila.HIDE_EMPTY_TANK;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ContainerProvider extends ProviderBase implements IServerDataProvider<BlockEntity> {
    public static final ContainerProvider INSTANCE = new ContainerProvider();

    private static final int BYTES_COLOR = 0xFFD400CD;

    public ContainerProvider() {
        super(modLoc("items"));
    }

    @Override
    protected void doAppendTooltip(CompoundTag tag, BlockAccessor accessor, IPluginConfig config) {
        // remove the fluid tooltip when it is completely empty.
        // for some reason jade will add an "Empty" tooltip when there is a FluidHandler but no fluid inside.
        if (config.get(HIDE_EMPTY_TANK) && tag.contains("jadeTanks", Tag.TAG_LIST)) {
            var listTag = tag.getList("jadeTanks", Tag.TAG_COMPOUND);
            if (listTag.size() == 1) {
                var tag1 = (CompoundTag) listTag.get(0);
                var fluid = FluidStack.loadFluidStackFromNBT(tag1);
                if (fluid.isEmpty()) {
                    tooltip.remove(VanillaPlugin.FORGE_FLUID);
                }
            }
        }

        if (config.get(ENHANCE_ITEMS) && tag.contains("tinactoryItems", Tag.TAG_LIST)) {
            // remove Jade tooltip first
            tooltip.remove(VanillaPlugin.INVENTORY);

            // use the same logic as Jade, but with support of 64+ items.
            var listTag = tag.getList("tinactoryItems", Tag.TAG_COMPOUND);
            var elements = new ArrayList<IElement>();
            var showName = listTag.size() < 5;
            var drawnCount = 0;
            var first = true;

            for (var itemTag : listTag) {
                if (!first && (showName || drawnCount >= JadeCommonConfig.inventoryShowItemPreLine)) {
                    add(elements);
                    elements.clear();
                    drawnCount = 0;
                }

                var stack = StackHelper.deserializeItemStack((CompoundTag) itemTag);
                if (showName) {
                    var stack1 = stack.copy();
                    stack1.setCount(1);
                    elements.add(Jade.smallItem(helper, stack1));
                    // jade does not use a TranslatableComponent
                    var text = new TextComponent(NUMBER_FORMAT.format(stack.getCount()))
                        .append("× ").append(stack.getHoverName());
                    elements.add(helper.text(text).message(null));
                } else {
                    elements.add(helper.item(stack));
                }

                ++drawnCount;
                first = false;
            }

            if (!elements.isEmpty()) {
                add(elements);
            }
        }

        if (config.get(BYTES) && tag.contains("tinactoryBytesUsed")) {
            var bytes = tag.getLong("tinactoryBytesUsed");
            var capacity = tag.getLong("tinactoryBytesCapacity");
            var text = tr("bytes", ClientUtil.getBytesString(bytes),
                ClientUtil.getBytesString(capacity));
            addProgress((float) bytes / (float) capacity, text, BYTES_COLOR);
        }
    }

    private void appendItems(CompoundTag tag, IItemHandler items, boolean showDetails) {
        var limit = showDetails ? JadeCommonConfig.inventoryDetailedShowAmount :
            JadeCommonConfig.inventoryNormalShowAmount;

        var listTag = new ListTag();
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

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level world,
        BlockEntity blockEntity, boolean showDetails) {
        if (JadeCommonConfig.shouldIgnoreTE(blockEntity.getType())) {
            return;
        }

        ITEM_HANDLER.tryGet(blockEntity).ifPresent(items -> appendItems(tag, items, showDetails));

        BYTES_PROVIDER.tryGet(blockEntity).ifPresent(bytes -> {
            tag.putLong("tinactoryBytesUsed", bytes.bytesUsed());
            tag.putLong("tinactoryBytesCapacity", bytes.bytesCapacity());
        });
    }
}
