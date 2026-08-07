package org.shsts.tinactory.core.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.builder.SimpleBuilder;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockLayer {
    private final List<String> rows;
    public final int minHeight;
    public final int maxHeight;

    public MultiblockLayer(Builder<?> builder) {
        this.rows = builder.rows;
        this.minHeight = builder.minHeight;
        this.maxHeight = builder.maxHeight;
    }

    public char get(int w, int d) {
        return rows.get(d).charAt(w);
    }

    public int width() {
        return rows.getFirst().length();
    }

    public int depth() {
        return rows.size();
    }

    public static class Builder<P> extends SimpleBuilder<MultiblockLayer, P, Builder<P>> {
        private final List<String> rows = new ArrayList<>();
        private int minHeight = 1;
        private int maxHeight = 1;

        public Builder(P parent) {
            super(parent);
        }

        public Builder<P> height(int val) {
            minHeight = val;
            maxHeight = val;
            return this;
        }

        // TODO: deal with the problem that the "try" test will modify property
        public Builder<P> height(int min, int max) {
            minHeight = min;
            maxHeight = max;
            return this;
        }

        public Builder<P> row(String str) {
            rows.add(str);
            return this;
        }

        private int checkWidth() {
            var width = 0;
            for (var row : rows) {
                if (row.isEmpty()) {
                    continue;
                }
                if (width == 0) {
                    width = row.length();
                } else if (width != row.length()) {
                    throw new IllegalArgumentException("layer rows are not same size");
                }
            }
            if (width == 0) {
                throw new IllegalArgumentException("has no row with width");
            }
            return width;
        }

        @Override
        protected MultiblockLayer createObject() {
            var width = checkWidth();
            var emptyRow = " ".repeat(width);
            for (var i = 0; i < rows.size(); i++) {
                if (rows.get(i).isEmpty()) {
                    rows.set(i, emptyRow);
                }
            }
            return new MultiblockLayer(this);
        }
    }
}
