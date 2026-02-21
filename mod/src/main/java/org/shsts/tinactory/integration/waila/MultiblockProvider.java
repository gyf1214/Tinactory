package org.shsts.tinactory.integration.waila;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;

import java.util.Optional;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.integration.waila.Waila.MULTIBLOCK;

public class MultiblockProvider extends ProviderBase implements IServerDataProvider<BlockEntity> {
    public static final MultiblockProvider INSTANCE = new MultiblockProvider();

    public MultiblockProvider() {
        super(modLoc("multiblock"));
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
            var name = Component.Serializer.fromJson(tag.getString("tinactoryMultiblockName"));
            add(helper.text(tr("multiblock", name)));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level world,
        BlockEntity blockEntity, boolean showDetails) {
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
                    tag.putString("tinactoryMultiblockName", Component.Serializer.toJson(name));
                }
            });
    }
}
