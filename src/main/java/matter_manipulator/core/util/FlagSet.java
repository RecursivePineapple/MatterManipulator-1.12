package matter_manipulator.core.util;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

public class FlagSet extends AbstractSet<Flag> {

    private final BitSet bits = new BitSet();

    public FlagSet() {
    }

    public FlagSet(Collection<Flag> flags) {
        for (Flag flag : flags) {
            bits.set(flag.id);
        }
    }

    public static FlagSet of(Flag... flags) {
        return new FlagSet(Arrays.asList(flags));
    }

    @Override
    public @NotNull Iterator<Flag> iterator() {
        return new Iterator<>() {

            private int bit = -1;

            @Override
            public boolean hasNext() {
                return bits.nextSetBit(bit) != -1;
            }

            @Override
            public Flag next() {
                int nextBit = bits.nextSetBit(bit);

                if (nextBit == -1) {
                    throw new NoSuchElementException();
                }

                this.bit = nextBit;

                return Flag.get(this.bit);
            }

            @Override
            public void remove() {
                bits.clear(this.bit);
            }
        };
    }

    @Override
    public void forEach(Consumer<? super Flag> action) {
        for (int bit = bits.nextSetBit(-1); bit != -1; bit = bits.nextSetBit(bit)) {
            action.accept(Flag.get(bit));
        }
    }

    @Override
    public boolean removeIf(@NotNull Predicate<? super Flag> filter) {
        boolean any = false;

        for (int bit = bits.nextSetBit(-1); bit != -1; bit = bits.nextSetBit(bit)) {
            if (filter.test(Flag.get(bit))) {
                bits.clear(bit);
                any = true;
            }
        }

        return any;
    }

    @Override
    public int size() {
        return bits.cardinality();
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Flag flag)) return false;

        return bits.get(flag.id);
    }
}
