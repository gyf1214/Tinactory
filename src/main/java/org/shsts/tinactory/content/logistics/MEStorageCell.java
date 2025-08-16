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
import org.shsts.tinactory.core.logistics.DigitalFluidStorage;
import org.shsts.tinactory.core.logistics.DigitalItemStorage;
import org.shsts.tinactory.core.logistics.DigitalStorage;
import org.shsts.tinactory.core.util.I18n;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.shsts.tinactory.content.AllCapabilities.FLUID_COLLECTION;
import static org.shsts.tinactory.content.AllCapabilities.ITEM_COLLECTION;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageCell extends CapabilityItem {
    private final boolean isFluid;
    private final int bytesLimit;

    public MEStorageCell(Properties properties, boolean isFluid, int bytesLimit) {
        super(properties.stacksTo(1));
        this.isFluid = isFluid;
        this.bytesLimit = bytesLimit;
    }

    public static Function<Properties, MEStorageCell> itemCell(int bytesLimit) {
        return properties -> new MEStorageCell(properties, false, bytesLimit);
    }

    public static Function<Properties, MEStorageCell> fluidCell(int bytesLimit) {
        return properties -> new MEStorageCell(properties, true, bytesLimit);
    }

    private Optional<DigitalStorage> getStorage(ItemStack stack) {
        if (isFluid) {
            return stack.getCapability(FLUID_COLLECTION.get())
                .<DigitalStorage>cast().resolve();
        } else {
            return stack.getCapability(ITEM_COLLECTION.get())
                .<DigitalStorage>cast().resolve();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world,
        List<Component> components, TooltipFlag isAdvanced) {
        getStorage(stack).ifPresent(storage -> {
            var amount = I18n.tr("tinactory.tooltip.meStorageCell",
                NUMBER_FORMAT.format(storage.bytesUsed()),
                NUMBER_FORMAT.format(storage.bytesLimit()));
            components.add(amount.withStyle(ChatFormatting.GRAY));
        });
    }

    @Override
    public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        var stack = event.getObject();
        if (isFluid) {
            event.addCapability(DigitalStorage.ID, new DigitalFluidStorage(stack, bytesLimit));
        } else {
            event.addCapability(DigitalStorage.ID, new DigitalItemStorage(stack, bytesLimit));
        }
    }
}
