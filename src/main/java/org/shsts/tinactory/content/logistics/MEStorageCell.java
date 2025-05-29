package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.shsts.tinactory.core.common.CapabilityItem;
import org.shsts.tinactory.core.logistics.DigitalItemStorage;
import org.shsts.tinactory.core.util.I18n;

import java.util.List;

import static org.shsts.tinactory.content.AllCapabilities.ITEM_COLLECTION;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageCell extends CapabilityItem {
    private final int bytesLimit;

    public MEStorageCell(Properties properties, int bytesLimit) {
        super(properties.stacksTo(1));
        this.bytesLimit = bytesLimit;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world,
        List<Component> components, TooltipFlag isAdvanced) {
        stack.getCapability(ITEM_COLLECTION.get()).ifPresent(cap -> {
            var storage = (DigitalItemStorage) cap;
            var amount = I18n.tr("tinactory.tooltip.meStorageCell",
                NUMBER_FORMAT.format(storage.bytesUsed()),
                NUMBER_FORMAT.format(storage.bytesLimit()));
            components.add(amount.withStyle(ChatFormatting.GRAY));
        });
    }

    @Override
    public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        event.addCapability(modLoc("logistics/me_storage_cell"),
            new DigitalItemStorage(bytesLimit));
    }
}
