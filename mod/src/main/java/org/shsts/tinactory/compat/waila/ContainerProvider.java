package org.shsts.tinactory.compat.waila;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.integration.util.ClientUtil;
import snownee.jade.addon.universal.FluidStorageProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

import static org.shsts.tinactory.AllCapabilities.BYTES_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.FLUID_HANDLER;
import static org.shsts.tinactory.compat.waila.Waila.BYTES;
import static org.shsts.tinactory.compat.waila.Waila.CONTAINER;
import static org.shsts.tinactory.compat.waila.Waila.HIDE_EMPTY_TANK;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ContainerProvider extends ProviderBase implements IServerDataProvider<BlockAccessor> {
    public static final ContainerProvider INSTANCE = new ContainerProvider();

    private static final int BYTES_COLOR = 0xFFD400CD;
    private static final String EMPTY_FLUID_KEY = "tinactoryEmptyFluidStorage";

    public ContainerProvider() {
        super(CONTAINER);
    }

    @Override
    protected void doAppendTooltip(CompoundTag tag, BlockAccessor accessor, IPluginConfig config) {
        // remove the fluid tooltip when it is completely empty.
        // for some reason jade will add an "Empty" tooltip when there is a FluidHandler but no fluid inside.
        if (config.get(HIDE_EMPTY_TANK) && tag.getBoolean(EMPTY_FLUID_KEY)) {
            tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE);
        }

        if (config.get(BYTES) && tag.contains("tinactoryBytesUsed")) {
            var bytes = tag.getLong("tinactoryBytesUsed");
            var capacity = tag.getLong("tinactoryBytesCapacity");
            var text = tr("bytes", ClientUtil.getBytesString(bytes),
                ClientUtil.getBytesString(capacity));
            addProgress((float) bytes / (float) capacity, text, BYTES_COLOR);
        }
    }

    @Override
    public int getDefaultPriority() {
        return FluidStorageProvider.getBlock().getDefaultPriority() + 1;
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        var blockEntity = accessor.getBlockEntity();
        if (blockEntity == null) {
            return;
        }

        FLUID_HANDLER.tryGet(blockEntity).ifPresent(fluids -> {
            var empty = fluids.getTanks() > 0;
            for (var i = 0; i < fluids.getTanks(); i++) {
                if (!fluids.getFluidInTank(i).isEmpty()) {
                    empty = false;
                    break;
                }
            }
            tag.putBoolean(EMPTY_FLUID_KEY, empty);
        });

        BYTES_PROVIDER.tryGet(blockEntity).ifPresent(bytes -> {
            tag.putLong("tinactoryBytesUsed", bytes.bytesUsed());
            tag.putLong("tinactoryBytesCapacity", bytes.bytesCapacity());
        });
    }

    @Override
    public ResourceLocation getUid() {
        return CONTAINER;
    }
}
