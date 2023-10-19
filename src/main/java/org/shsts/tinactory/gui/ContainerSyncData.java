package org.shsts.tinactory.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.inventory.ContainerData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ContainerSyncData<U, M extends ContainerMenu<?>> implements ContainerData {
    protected final M menu;
    protected final int slots;
    public U value;

    public ContainerSyncData(M menu, int slots, U defaultValue) {
        this.menu = menu;
        this.slots = slots;
        this.value = defaultValue;
    }

    @Override
    public int getCount() {
        return this.slots;
    }

    protected abstract int encode(int slot);

    protected abstract void decode(int slot, int data);

    protected void updateValue() {}

    protected void onReceive() {}

    @Override
    public int get(int slot) {
        this.updateValue();
        return this.encode(slot);
    }

    @Override
    public void set(int slot, int data) {
        this.decode(slot, data);
        this.onReceive();
    }

    // be aware that DataSlot can only hold short.
    private static class Simple<M1 extends ContainerMenu<?>> extends ContainerSyncData<Short, M1> {
        public Simple(M1 menu) {
            super(menu, 1, (short) 0);
        }

        @Override
        protected int encode(int slot) {
            return this.value;
        }

        @Override
        protected void decode(int slot, int data) {
            this.value = (short) data;
        }
    }

    public static <M extends ContainerMenu<?>> ContainerSyncData<Short, M> simple(M menu) {
        return new Simple<>(menu);
    }

    public static <M extends ContainerMenu<?>> ContainerSyncData<Short, M>
    simpleReader(M menu, Function<M, Short> reader) {
        return new Simple<>(menu) {
            @Override
            protected void updateValue() {
                this.value = reader.apply(this.menu);
                assert this.value != null;
            }
        };
    }
}
