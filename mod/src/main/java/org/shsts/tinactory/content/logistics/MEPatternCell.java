package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.shsts.tinactory.core.autocraft.api.MachineConstraintRegistry;
import org.shsts.tinactory.core.autocraft.integration.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.integration.PatternNbtCodec;
import org.shsts.tinactory.core.autocraft.integration.PatternCellPortState;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.common.CapabilityItem;
import org.shsts.tinactory.core.common.ItemCapabilityProvider;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import static org.shsts.tinactory.AllCapabilities.PATTERN_CELL;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.addTooltip;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternCell extends CapabilityItem {
    private static final ResourceLocation ID = modLoc("logistics/me_pattern_cell");
    public static final int BYTES_PER_PATTERN = PatternCellPortState.BYTES_PER_PATTERN;

    private final int bytesLimit;

    public MEPatternCell(Properties properties, int bytesLimit) {
        super(properties.stacksTo(1));
        this.bytesLimit = bytesLimit;
    }

    public static Function<Properties, MEPatternCell> patternCell(int bytesLimit) {
        return properties -> new MEPatternCell(properties, bytesLimit);
    }

    public int bytesCapacity() {
        return bytesLimit;
    }

    public int bytesUsed(ItemStack stack, PatternNbtCodec codec) {
        return patternPort(stack).bytesUsed();
    }

    public int patternCount(ItemStack stack, PatternNbtCodec codec) {
        return patternPort(stack).patterns().size();
    }

    public List<CraftPattern> listPatterns(ItemStack stack, PatternNbtCodec codec) {
        return patternPort(stack).patterns();
    }

    public boolean insertPattern(ItemStack stack, CraftPattern pattern, PatternNbtCodec codec) {
        if (!(stack.getItem() instanceof MEPatternCell)) {
            return false;
        }
        return patternPort(stack).insert(pattern);
    }

    public IPatternCellPort patternPort(ItemStack stack) {
        return stack.getCapability(PATTERN_CELL.get()).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        @Nullable Level world,
        List<Component> tooltip,
        TooltipFlag isAdvanced) {
        var count = patternCount(stack, new PatternNbtCodec(new MachineConstraintRegistry()));
        var used = bytesUsed(stack, new PatternNbtCodec(new MachineConstraintRegistry()));
        addTooltip(tooltip, "mePatternCell",
            NUMBER_FORMAT.format(count),
            NUMBER_FORMAT.format(used),
            NUMBER_FORMAT.format(bytesLimit));
    }

    @Override
    public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        event.addCapability(ID, new PatternCapability(event.getObject(), bytesLimit));
    }

    private static final class PatternCapability extends ItemCapabilityProvider implements IPatternCellPort {
        private final PatternCellPortState state;
        private final LazyOptional<IPatternCellPort> patternCap;

        private PatternCapability(ItemStack stack, int bytesLimit) {
            super(stack, ID);
            this.state = new PatternCellPortState(bytesLimit);
            this.patternCap = LazyOptional.of(() -> this);
        }

        @Override
        public int bytesCapacity() {
            return state.bytesCapacity();
        }

        @Override
        public int bytesUsed() {
            return state.bytesUsed();
        }

        @Override
        public List<CraftPattern> patterns() {
            return state.patterns();
        }

        @Override
        public boolean insert(CraftPattern pattern) {
            if (!state.insert(pattern)) {
                return false;
            }
            syncTag();
            return true;
        }

        @Override
        public boolean remove(String patternId) {
            if (!state.remove(patternId)) {
                return false;
            }
            syncTag();
            return true;
        }

        @Override
        protected CompoundTag serializeNBT() {
            return state.serialize();
        }

        @Override
        protected void deserializeNBT(CompoundTag tag) {
            state.deserialize(tag);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            if (cap == PATTERN_CELL.get()) {
                return patternCap.cast();
            }
            return LazyOptional.empty();
        }
    }
}
