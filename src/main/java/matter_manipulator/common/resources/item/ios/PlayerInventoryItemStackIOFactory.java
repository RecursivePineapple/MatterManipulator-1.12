package matter_manipulator.common.resources.item.ios;

import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.resources.item.ItemHandlerIterator;
import matter_manipulator.core.context.PlayerContext;
import matter_manipulator.core.item.ItemStackIO;
import matter_manipulator.core.item.ItemStackIterator;
import matter_manipulator.core.item.ItemStackIteratorBuilder;
import matter_manipulator.core.item.ItemStackPredicate;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.resources.IteratorUsage;
import matter_manipulator.core.resources.ResourceIOFactory;

public class PlayerInventoryItemStackIOFactory implements ResourceIOFactory<ItemStackIO> {

    public static final PlayerInventoryItemStackIOFactory INSTANCE = new PlayerInventoryItemStackIOFactory();

    @Override
    public Optional<ItemStackIO> getIO(PlayerContext context, IDataStorage storage) {
        return Optional.of(createIO(context.getRealPlayer()));
    }

    public @NotNull ItemStackIO createIO(EntityPlayer player) {
        NonNullList<ItemStack> inv = player.inventory.mainInventory;

        return new ItemStackIO() {

            @Override
            @NotNull
            public ItemStackIteratorBuilder iterator() {
                return new ItemStackIteratorBuilder() {

                    private ItemStackPredicate filter;
                    private boolean patterns;

                    @Override
                    public ItemStackIteratorBuilder setItemFilter(@Nullable ItemStackPredicate filter) {
                        this.filter = filter;
                        return this;
                    }

                    @Override
                    public ItemStackIteratorBuilder iteratePatterns(boolean onlyPatterns) {
                        this.patterns = onlyPatterns;
                        return this;
                    }

                    @Override
                    public ItemStackIteratorBuilder setUsage(@NotNull IteratorUsage usage) {
                        return this;
                    }

                    @Override
                    public ItemStackIterator build() {
                        if (patterns) return ItemStackIterator.EMPTY;

                        return new ItemHandlerIterator(new ItemStackHandler(inv), filter);
                    }
                };
            }
        };
    }
}
