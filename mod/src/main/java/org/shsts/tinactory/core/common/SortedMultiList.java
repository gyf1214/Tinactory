package org.shsts.tinactory.core.common;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.Comparator;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SortedMultiList<T> {
    private final Random random = new Random();

    private final Comparator<? super T> comparator;
    @Nullable
    private Node root;

    public SortedMultiList(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }

    public record IndexedValue<T>(T value, int index, int count) {}

    private class Node {
        T key;

        int count;
        int size;
        int priority = random.nextInt();

        @Nullable
        Node left = null;
        @Nullable
        Node right = null;

        Node(T key, int count) {
            this.key = key;
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
        root = insert(root, key, count);
    }

    public void remove(T key, int count) {
        root = remove(root, key, count);
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

    private Node insert(@Nullable Node node, T key, int count) {
        if (node == null) {
            return new Node(key, count);
        }

        int cmp = comparator.compare(key, node.key);
        if (cmp == 0) {
            node.count++;
        } else if (cmp < 0) {
            node.left = insert(node.left, key, count);
            if (node.left.priority > node.priority) {
                node = rotateRight(node, node.left);
            }
        } else {
            node.right = insert(node.right, key, count);
            if (node.right.priority > node.priority) {
                node = rotateLeft(node, node.right);
            }
        }

        update(node);
        return node;
    }

    @Nullable
    private Node remove(@Nullable Node node, T key, int count) {
        if (node == null) {
            return null;
        }

        int cmp = comparator.compare(key, node.key);

        if (cmp < 0) {
            node.left = remove(node.left, key, count);
        } else if (cmp > 0) {
            node.right = remove(node.right, key, count);
        } else {
            if (node.count > count) {
                node.count -= count;
            } else {
                return merge(node.left, node.right);
            }
        }

        update(node);
        return node;
    }

    public void clear() {
        root = null;
    }
}
