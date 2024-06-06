package org.shsts.tinactory.core.util;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RoundRobinList<T> extends ArrayList<T> {
    private final AtomicInteger nextIndex = new AtomicInteger();


    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int count = size();
            private int first = nextIndex.getAndAdd(1);

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

    public T getNext() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        var next = nextIndex.getAndAdd(1);
        return get(next % size());
    }
}
