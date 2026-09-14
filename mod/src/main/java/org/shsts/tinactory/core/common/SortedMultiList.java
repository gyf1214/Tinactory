package org.shsts.tinactory.core.common;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SortedMultiList<T> {
    private final Random random = new Random();

    private final Map<T, Integer> uniqueKey = new HashMap<>();
    private final Comparator<? super T> comparator;
    @Nullable
    private Node root;
    private int uniqueKeys = 0;

    public SortedMultiList(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }

    public record IndexedValue<T>(T value, int index, int count) {}

    private class Node {
        public final T key;
        public final int unique;
        public final int priority = random.nextInt();

        public int count;
        public int size;

        @Nullable
        public Node left = null;
        @Nullable
        public Node right = null;

        public Node(T key, int count, int unique) {
            this.key = key;
            this.unique = unique;
            this.count = count;
            this.size = count;
        }
    }

    private int size(@Nullable Node node) {
        return node == null ? 0 : node.size;
    }

    public int size() {
        return size(root);
    }

    public void insert(T key, int count) {
        var unique = uniqueKey.computeIfAbsent(key, $ -> uniqueKeys++);
        root = insert(root, key, count, unique);
    }

    public void remove(T key, int count) {
        if (!uniqueKey.containsKey(key)) {
            return;
        }
        root = remove(root, key, count, uniqueKey.get(key));
    }

    @Nullable
    public IndexedValue<T> get(int index) {
        if (index < 0 || index >= size()) {
            return null;
        }

        var node = root;
        while (node != null) {
            int leftSize = size(node.left);
            if (index < leftSize) {
                node = node.left;
            } else if (index < leftSize + node.count) {
                return new IndexedValue<>(node.key, index - leftSize, node.count);
            } else {
                index -= leftSize + node.count;
                node = node.right;
            }
        }

        throw new AssertionError();
    }

    private void update(@Nullable Node node) {
        if (node != null) {
            node.size = size(node.left) + node.count + size(node.right);
        }
    }

    private Node rotateLeft(Node node, Node newRoot) {
        node.right = newRoot.left;
        newRoot.left = node;
        update(node);
        update(newRoot);
        return newRoot;
    }

    private Node rotateRight(Node node, Node newRoot) {
        node.left = newRoot.right;
        newRoot.right = node;
        update(node);
        update(newRoot);
        return newRoot;
    }

    private @Nullable Node merge(@Nullable Node left, @Nullable Node right) {
        if (left == null) {
            return right;
        }

        if (right == null) {
            return left;
        }

        if (left.priority > right.priority) {
            left.right = merge(left.right, right);
            update(left);
            return left;
        } else {
            right.left = merge(left, right.left);
            update(right);
            return right;
        }
    }

    private int compare(Node node, T key, int unique) {
        if (node.unique == unique) {
            return 0;
        }
        var cmp1 = comparator.compare(key, node.key);
        return cmp1 != 0 ? cmp1 : (unique < node.unique ? -1 : 1);
    }

    private Node insert(@Nullable Node node, T key, int count, int unique) {
        if (node == null) {
            return new Node(key, count, unique);
        }

        int cmp = compare(node, key, unique);
        if (cmp == 0) {
            node.count += count;
        } else if (cmp < 0) {
            node.left = insert(node.left, key, count, unique);
            if (node.left.priority > node.priority) {
                node = rotateRight(node, node.left);
            }
        } else {
            node.right = insert(node.right, key, count, unique);
            if (node.right.priority > node.priority) {
                node = rotateLeft(node, node.right);
            }
        }

        update(node);
        return node;
    }

    @Nullable
    private Node remove(@Nullable Node node, T key, int count, int unique) {
        if (node == null) {
            return null;
        }

        int cmp = compare(node, key, unique);

        if (cmp < 0) {
            node.left = remove(node.left, key, count, unique);
        } else if (cmp > 0) {
            node.right = remove(node.right, key, count, unique);
        } else {
            if (node.count > count) {
                node.count -= count;
            } else {
                uniqueKey.remove(key);
                return merge(node.left, node.right);
            }
        }

        update(node);
        return node;
    }

    public void clear() {
        root = null;
        uniqueKey.clear();
    }
}
