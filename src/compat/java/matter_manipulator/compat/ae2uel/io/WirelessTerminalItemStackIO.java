package matter_manipulator.compat.ae2uel.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import appeng.api.config.Actionable;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.compat.ae2uel.util.AEItemStackWrapper;
import matter_manipulator.core.item.ImmutableItemStack;
import matter_manipulator.core.item.ItemStackIO;
import matter_manipulator.core.item.ItemStackIterator;
import matter_manipulator.core.item.ItemStackIteratorBuilder;
import matter_manipulator.core.item.ItemStackPredicate;
import matter_manipulator.core.resources.IteratorUsage;

@Desugar
public record WirelessTerminalItemStackIO(WirelessTerminalState terminal) implements ItemStackIO {

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
                List<IAEItemStack> stacks;

                if (filter != null) {
                    if (filter.getStacks() != null) {
                        stacks = DataUtils.mapToList(
                            filter.getStacks(),
                            f -> {
                                ItemStack stack = f.toStack(1);

                                if (stack.isEmpty()) return null;

                                return terminal.extractItem(AEItemStack.fromItemStack(stack), Actionable.SIMULATE);
                            });
                    } else {
                        var stored = terminal.items().getStorageList();

                        stacks = new ArrayList<>(stored.size());

                        AEItemStackWrapper pooled = new AEItemStackWrapper(null);

                        for (var stack : stored) {
                            if (!filter.test(pooled.set(stack))) continue;

                            stacks.add(stack);
                        }
                    }
                } else {
                    stacks = ObjectIterators.pour(terminal.items().getStorageList()
                        .iterator());
                }

                stacks.removeIf(Objects::isNull);

                if (onlyPatterns) {
                    stacks.removeIf(stack -> !stack.isCraftable());
                }

                return stacks.isEmpty() ? ItemStackIterator.EMPTY : new Iter(terminal, stacks, filter);
            }
        };
    }

    private static class Iter implements ItemStackIterator {

        private final WirelessTerminalState terminal;
        private final int count;
        private final List<IAEItemStack> stacks;
        private final ItemStackPredicate filter;

        private final AEItemStackWrapper pooled = new AEItemStackWrapper(null);

        private int i;
        private int last;

        public Iter(WirelessTerminalState terminal, List<IAEItemStack> stacks, ItemStackPredicate filter) {
            this.terminal = terminal;
            this.count = stacks.size();
            this.stacks = stacks;
            this.filter = filter;
        }

        public boolean hasNext() {
            return this.i < count;
        }

        public @NotNull ImmutableItemStack next() {
            this.last = this.i++;

            this.pooled.set(this.stacks.get(this.last));

            IAEItemStack target = this.stacks.get(this.last);
            IAEItemStack contained = terminal.extractItem(target, Actionable.SIMULATE);

            pooled.set(contained);

            return filter.test(pooled) ? pooled : ImmutableItemStack.EMPTY;
        }

        public boolean hasPrevious() {
            return this.i > 0;
        }

        public @NotNull ImmutableItemStack previous() {
            this.last = --this.i;

            this.pooled.set(this.stacks.get(this.last));

            IAEItemStack target = this.stacks.get(this.last);
            IAEItemStack contained = terminal.extractItem(target, Actionable.SIMULATE);

            pooled.set(contained);

            return filter.test(pooled) ? pooled : ImmutableItemStack.EMPTY;
        }

        public int nextIndex() {
            return this.i + 1;
        }

        public int previousIndex() {
            return this.i - 1;
        }

        @Override
        public @NotNull ItemStack extract(int amount, boolean force) {
            IAEItemStack target = this.stacks.get(this.last);
            IAEItemStack extracted = terminal.extractItem(target.empty().setStackSize(amount), Actionable.MODULATE);
            return extracted.createItemStack();
        }

        @Override
        public int insert(ImmutableItemStack stack, boolean force) {
            IAEItemStack insert = AEItemStack.fromItemStack(stack.toStack());

            if (insert == null) return 0;

            IAEItemStack rejected = terminal.insertItem(insert, Actionable.MODULATE);

            return rejected == null ? 0 : MathUtils.longToInt(rejected.getStackSize());
        }

        public boolean rewind() {
            this.i = 0;
            return true;
        }
    }

    @Override
    public @NotNull ItemStack pull(@Nullable ItemStackPredicate filter, int amount) {
        AEItemStackWrapper wrapper = new AEItemStackWrapper(null);

        if (filter != null && filter.getStacks() != null) {
            for (var filterStack : filter.getStacks()) {
                IAEItemStack test = AEItemStack.fromItemStack(filterStack.toStack(1)).setStackSize(amount);

                IAEItemStack stored = this.terminal.extractItem(test, Actionable.SIMULATE);

                if (!filter.test(wrapper.set(stored))) continue;

                if (stored.getStackSize() == amount) {
                    IAEItemStack extracted = this.terminal.extractItem(test, Actionable.MODULATE);

                    if (extracted.getStackSize() != amount) {
                        this.terminal.items().injectItems(extracted, Actionable.MODULATE, this.terminal.source());
                    } else {
                        return extracted.createItemStack();
                    }
                }
            }
        }

        for (var stored : this.terminal.items().getStorageList()) {
            if (filter != null) {
                if (!filter.test(wrapper.set(stored))) continue;
            }

            if (stored.getStackSize() >= amount) {
                IAEItemStack extracted = this.terminal.extractItem(stored.empty().setStackSize(amount), Actionable.MODULATE);

                if (extracted.getStackSize() != amount) {
                    this.terminal.items().injectItems(extracted, Actionable.MODULATE, this.terminal.source());
                } else {
                    return extracted.createItemStack();
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int store(ImmutableItemStack stack) {
        IAEItemStack ae = AEItemStack.fromItemStack(stack.toStack());

        IAEItemStack rejected = this.terminal.insertItem(ae, Actionable.MODULATE);

        return rejected == null ? 0 : MathUtils.longToInt(rejected.getStackSize());
    }
}
