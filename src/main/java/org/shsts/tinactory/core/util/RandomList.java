package org.shsts.tinactory.core.util;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RandomList<T> extends ArrayList<T> {
    private final Random random = new Random();

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int count = size();
            private int first = count > 0 ? (random.nextInt() % count + count) % count : 0;

            @Override
            public boolean hasNext() {
                return count > 0;
            }

            @Override
            public T next() {
                count--;
                return get(first++ % size());
            }
        };
    }
}
