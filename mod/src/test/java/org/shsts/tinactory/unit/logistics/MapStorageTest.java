package org.shsts.tinactory.unit.logistics;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.logistics.IStackAdapter;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.gui.ItemIdRenderDescriptor;
import org.shsts.tinactory.core.logistics.MapStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapStorageTest {
    private static final IStackAdapter<MutableStack> ADAPTER = new IStackAdapter<>() {
        @Override
        public MutableStack empty() {
            return new MutableStack("", 0);
        }

        @Override
        public boolean isEmpty(MutableStack stack) {
            return stack.amount <= 0;
        }

        @Override
        public MutableStack copy(MutableStack stack) {
            return new MutableStack(stack.id, stack.amount);
        }

        @Override
        public int amount(MutableStack stack) {
            return stack.amount;
        }

        @Override
        public MutableStack withAmount(MutableStack stack, int amount) {
            return new MutableStack(stack.id, amount);
        }

        @Override
        public boolean canStack(MutableStack left, MutableStack right) {
            return left.id.equals(right.id);
        }

        @Override
        public IStackKey keyOf(MutableStack stack) {
            return new MutableKey(stack.id);
        }

        @Override
        public MutableStack stackOf(IStackKey key, long amount) {
            return new MutableStack(((MutableKey) key).id, Math.toIntExact(amount));
        }

        @Override
        public IRenderDescriptor display(MutableStack stack) {
            return new ItemIdRenderDescriptor(ResourceLocation.parse(stack.id));
        }

        @Override
        public Component name(MutableStack stack) {
            return Component.literal(stack.id);
        }

        @Override
        public Optional<List<Component>> tooltip(MutableStack stack) {
            return Optional.of(List.of(name(stack)));
        }
    };

    @Test
    void shouldReturnDetachedStackWhenSimulatingSpecificFullExtraction() {
        var storage = new TestStorage();
        storage.insert(new MutableStack("iron", 4), false);

        var extracted = storage.extract(new MutableStack("iron", 4), true);
        extracted.setAmount(0);

        assertEquals(4, storage.getStorageAmount(new MutableStack("iron", 1)));
    }

    @Test
    void shouldReturnDetachedStackWhenSimulatingFullAnyExtraction() {
        var storage = new TestStorage();
        storage.insert(new MutableStack("iron", 4), false);

        var extracted = storage.extract(4, true);
        extracted.setAmount(0);

        assertEquals(4, storage.getStorageAmount(new MutableStack("iron", 1)));
    }

    private static final class TestStorage extends MapStorage<MutableStack> {
        private TestStorage() {
            super(ADAPTER);
        }

        @Override
        protected boolean acceptInput(IStackKey key, int existingAmount) {
            return true;
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        protected int insertLimit(IStackKey key, int existingAmount) {
            return Integer.MAX_VALUE;
        }

        @Override
        protected void postInsert(IStackKey key, int amount, int existingAmount) {}

        @Override
        protected void postExtract(IStackKey key, int amount, int existingAmount) {}

        @Override
        protected CompoundTag serializeStack(HolderLookup.Provider provider, MutableStack stack) {
            var tag = new CompoundTag();
            tag.putString("id", stack.id);
            tag.putInt("amount", stack.amount);
            return tag;
        }

        @Override
        protected MutableStack deserializeStack(HolderLookup.Provider provider, CompoundTag tag) {
            return new MutableStack(tag.getString("id"), tag.getInt("amount"));
        }
    }

    private static final class MutableStack {
        private final String id;
        private int amount;

        private MutableStack(String id, int amount) {
            this.id = id;
            this.amount = amount;
        }

        private void setAmount(int amount) {
            this.amount = amount;
        }
    }

    private record MutableKey(String id) implements IStackKey {
        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public ResourceLocation loc() {
            return ResourceLocation.parse(id);
        }

        @Override
        public IStackAdapter<?> adapter() {
            return ADAPTER;
        }
    }
}
