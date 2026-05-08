package matter_manipulator.compat.emc.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.core.item.FastImmutableItemStack;
import matter_manipulator.core.item.ImmutableItemStack;
import matter_manipulator.core.item.InsertionItemStack;
import matter_manipulator.core.item.ItemStackIO;
import matter_manipulator.core.item.ItemStackIterator;
import matter_manipulator.core.item.ItemStackIteratorBuilder;
import matter_manipulator.core.item.ItemStackPredicate;
import matter_manipulator.core.item.ItemUtils;
import matter_manipulator.core.resources.IteratorUsage;

@Desugar
public record ArcaneTabletItemStackIO(ArcaneTabletState state) implements ItemStackIO {

    @Override
    public @NotNull ItemStack pull(@Nullable ItemStackPredicate filter, int amount) {
        if (filter == null) return ItemStack.EMPTY;

        var stacks = filter.getStacks();

        if (stacks != null) {
            for (var stack : stacks) {
                ItemStack mcStack = stack.toStackFast(1);

                long available = state.getAvailable(mcStack);

                if (available >= amount) {
                    long extracted = state.extract(mcStack, amount);

                    return ItemUtils.copyWithAmount(mcStack, MathUtils.longToInt(extracted));
                }
            }
        } else {
            FastImmutableItemStack pooled = new FastImmutableItemStack();

            for (var stack : state.knownStacks()) {
                if (filter.test(pooled.set(stack))) {
                    long available = state.getAvailable(stack);

                    if (available >= amount) {
                        long extracted = state.extract(stack, amount);

                        return ItemUtils.copyWithAmount(stack, MathUtils.longToInt(extracted));
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int store(ImmutableItemStack stack) {
        if (state.insert(stack.toStackFast(1), stack.getCount())) {
            return 0;
        } else {
            return stack.getCount();
        }
    }

    @Override
    public @NotNull ItemStackIteratorBuilder iterator() {
        return new ItemStackIteratorBuilder() {

            private ItemStackPredicate filter;
            private boolean onlyPatterns;

            @Override
            public ItemStackIteratorBuilder setItemFilter(@Nullable ItemStackPredicate filter) {
                this.filter = filter;
                return this;
            }

            @Override
            public ItemStackIteratorBuilder iteratePatterns(boolean onlyPatterns) {
                this.onlyPatterns = onlyPatterns;
                return this;
            }

            @Override
            public ItemStackIteratorBuilder setUsage(@NotNull IteratorUsage usage) {
                return this;
            }

            @Override
            public ItemStackIterator build() {
                if (onlyPatterns) {
                    return ItemStackIterator.EMPTY;
                }

                List<ItemStack> stacks;

                if (filter != null) {
                    if (filter.getStacks() != null) {
                        stacks = DataUtils.mapToList(
                            filter.getStacks(),
                            f -> {
                                ItemStack stack = f.toStack(1);

                                if (stack.isEmpty()) return null;

                                long available = state.getAvailable(stack);

                                return ItemUtils.copyWithAmount(stack, MathUtils.longToInt(available));
                            });
                    } else {
                        FastImmutableItemStack pooled = new FastImmutableItemStack();

                        stacks = new ArrayList<>();

                        for (var stack : state.knownStacks()) {
                            if (!filter.test(pooled.set(stack))) continue;

                            long available = state.getAvailable(stack);

                            if (available <= 0) continue;

                            stacks.add(ItemUtils.copyWithAmount(stack, MathUtils.longToInt(available)));
                        }
                    }
                } else {
                    stacks = ObjectIterators.pour(state.knownStacks().iterator());
                }

                stacks.removeIf(Objects::isNull);
                stacks.removeIf(ItemStack::isEmpty);

                return stacks.isEmpty() ? ItemStackIterator.EMPTY : new Iter(state, stacks, filter);
            }
        };
    }

    private static class Iter implements ItemStackIterator {

        private final ArcaneTabletState state;
        private final int count;
        private final List<ItemStack> stacks;
        private final ItemStackPredicate filter;

        private final InsertionItemStack pooled = new InsertionItemStack();

        private int i;
        private int last;

        public Iter(ArcaneTabletState state, List<ItemStack> stacks, ItemStackPredicate filter) {
            this.state = state;
            this.count = stacks.size();
            this.stacks = stacks;
            this.filter = filter;
        }

        @Override
        public boolean hasNext() {
            return this.i < count;
        }

        @Override
        public @NotNull ImmutableItemStack next() {
            this.last = this.i++;

            ItemStack stack = this.stacks.get(this.last);

            long available = state.getAvailable(stack);

            pooled.set(stack, MathUtils.longToInt(available));

            return filter.test(pooled) ? pooled : ImmutableItemStack.EMPTY;
        }

        @Override
        public boolean hasPrevious() {
            return this.i > 0;
        }

        @Override
        public @NotNull ImmutableItemStack previous() {
            this.last = --this.i;

            ItemStack stack = this.stacks.get(this.last);

            long available = state.getAvailable(stack);

            pooled.set(stack, MathUtils.longToInt(available));

            return filter.test(pooled) ? pooled : ImmutableItemStack.EMPTY;
        }

        @Override
        public int nextIndex() {
            return this.i + 1;
        }

        @Override
        public int previousIndex() {
            return this.i - 1;
        }

        @Override
        public @NotNull ItemStack extract(int amount, boolean force) {
            ItemStack stack = this.stacks.get(this.last);

            long extracted = state.extract(stack, amount);

            return ItemUtils.copyWithAmount(stack, MathUtils.longToInt(extracted));
        }

        @Override
        public int insert(ImmutableItemStack stack, boolean force) {
            if (state.insert(stack.toStackFast())) {
                return 0;
            } else {
                return stack.getCount();
            }
        }

        public boolean rewind() {
            this.i = 0;
            return true;
        }
    }
}
