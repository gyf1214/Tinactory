package org.shsts.tinactory.compat.waila;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.integration.multiblock.Multiblock;
import org.shsts.tinactory.integration.multiblock.MultiblockInterface;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

import java.util.Optional;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.compat.waila.Waila.MULTIBLOCK;

public class MultiblockProvider extends ProviderBase implements IServerDataProvider<BlockAccessor> {
    public static final MultiblockProvider INSTANCE = new MultiblockProvider();

    public MultiblockProvider() {
        super(MULTIBLOCK);
    }

    @Override
    protected void doAppendTooltip(CompoundTag tag, BlockAccessor accessor, IPluginConfig config) {
        if (!config.get(MULTIBLOCK)) {
            return;
        }

        if (tag.contains("tinactoryMultiblockInvalid", Tag.TAG_BYTE) &&
            tag.getBoolean("tinactoryMultiblockInvalid")) {
            add(helper.text(tr("invalidMultiblock")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.RED)));
        } else if (tag.contains("tinactoryMultiblockName", Tag.TAG_STRING)) {
            var name = Component.Serializer.fromJson(tag.getString("tinactoryMultiblockName"),
                accessor.getLevel().registryAccess());
            add(helper.text(tr("multiblock", name)));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        var blockEntity = accessor.getBlockEntity();
        if (blockEntity == null) {
            return;
        }

        Multiblock.tryGet(blockEntity).ifPresent(multiblock -> {
            if (multiblock.getInterface().isEmpty()) {
                tag.putBoolean("tinactoryMultiblockInvalid", true);
            }
        });

        MACHINE.tryGet(blockEntity)
            .flatMap($ -> $ instanceof MultiblockInterface $1 ? Optional.of($1) : Optional.empty())
            .ifPresent(multiblockInterface -> {
                var multiblock = multiblockInterface.getMultiblock();
                if (multiblock.isEmpty()) {
                    tag.putBoolean("tinactoryMultiblockInvalid", true);
                } else {
                    var name = multiblock.get().blockEntity.getBlockState().getBlock().getName();
                    tag.putString("tinactoryMultiblockName", Component.Serializer.toJson(name,
                        accessor.getLevel().registryAccess()));
                }
            });
    }

    @Override
    public ResourceLocation getUid() {
        return MULTIBLOCK;
    }
}
