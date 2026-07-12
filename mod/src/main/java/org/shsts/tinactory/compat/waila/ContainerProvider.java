package org.shsts.tinactory.compat.waila;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.integration.util.ClientUtil;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

import static org.shsts.tinactory.AllCapabilities.BYTES_PROVIDER;
import static org.shsts.tinactory.compat.waila.Waila.BYTES;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ContainerProvider extends ProviderBase implements IServerDataProvider<BlockAccessor> {
    public static final ContainerProvider INSTANCE = new ContainerProvider();

    private static final int BYTES_COLOR = 0xFFD400CD;

    public ContainerProvider() {
        super(BYTES);
    }

    @Override
    protected void doAppendTooltip(CompoundTag tag, BlockAccessor accessor, IPluginConfig config) {
        if (config.get(BYTES) && tag.contains("tinactoryBytesUsed")) {
            var bytes = tag.getLong("tinactoryBytesUsed");
            var capacity = tag.getLong("tinactoryBytesCapacity");
            var text = tr("bytes", ClientUtil.getBytesString(bytes),
                ClientUtil.getBytesString(capacity));
            addProgress((float) bytes / (float) capacity, text, BYTES_COLOR);
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        var blockEntity = accessor.getBlockEntity();
        if (blockEntity == null) {
            return;
        }

        BYTES_PROVIDER.tryGet(blockEntity).ifPresent(bytes -> {
            tag.putLong("tinactoryBytesUsed", bytes.bytesUsed());
            tag.putLong("tinactoryBytesCapacity", bytes.bytesCapacity());
        });
    }

    @Override
    public ResourceLocation getUid() {
        return BYTES;
    }
}
