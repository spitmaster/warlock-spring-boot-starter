package io.github.spitmaster.warlock.util;

import java.util.Objects;

//refer to jdk.internal.vm.compiler.collections.Pair
public class Pair<L, R> {

    public final L left;

    public final R right;

    private Pair(final L left, final R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return new Pair<>(left, right);
    }

    public L getLeft() {
        return this.left;
    }

    public R getRight() {
        return this.right;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.left) + 31 * Objects.hashCode(this.right);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Pair) {
            Pair pair = (Pair) obj;
            return Objects.equals(this.left, pair.left) && Objects.equals(this.right, pair.right);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", this.left, this.right);
    }

}
