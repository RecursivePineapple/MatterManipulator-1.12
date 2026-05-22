package matter_manipulator.common.structure;

import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.utils.data.Lazy;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.context.StructureContext;
import matter_manipulator.core.context.StructureInteractContext;
import matter_manipulator.core.meta.MetaKey;

public class StructureUtils {

    private static final StructureElement<?> AIR = spec(BlockSpec::air);

    public interface BlockSpecSupplier extends Supplier<BlockSpec> {

    }

    public static <T> StructureElement<T> spec(BlockSpecSupplier supplier) {
        return lazy(() -> new BlockSpecStructureElement<>(supplier.get()));
    }

    public static <T> StructureElement<T> lazy(Supplier<StructureElement<T>> next) {
        Lazy<StructureElement<T>> element = new Lazy<>(next);

        return new StructureElement<>() {

            @Override
            public <K> K getMetadata(MetaKey<K> key) {
                return element.get().getMetadata(key);
            }

            @Override
            public boolean check(StructureContext<? extends T> context, BlockPos pos) {
                return element.get().check(context, pos);
            }

            @Override
            public boolean build(StructureInteractContext<? extends T> context, BlockPos pos) {
                return element.get().build(context, pos);
            }

            @Override
            public void emitHint(StructureInteractContext<? extends T> context, BlockPos pos) {
                element.get().emitHint(context, pos);
            }
        };
    }

    public static <T> StructureElement<T> air() {
        //noinspection unchecked
        return (StructureElement<T>) AIR;
    }

    @SafeVarargs
    public static <T> StructureElement<T> chain(StructureElement<T>... elements) {
        return new StructureElement<>() {

            @Override
            public @Nullable <K> K getMetadata(MetaKey<K> key) {
                for (var e : elements) {
                    var meta = e.getMetadata(key);

                    if (meta != null) return meta;
                }

                return null;
            }

            @Override
            public boolean check(StructureContext<? extends T> context, BlockPos pos) {
                for (var e : elements) {
                    if (e.check(context, pos)) return true;
                }

                return false;
            }

            @Override
            public boolean build(StructureInteractContext<? extends T> context, BlockPos pos) {
                for (var e : elements) {
                    if (e.build(context, pos)) return true;
                }

                return false;
            }

            @Override
            public void emitHint(StructureInteractContext<? extends T> context, BlockPos pos) {
                elements[0].emitHint(context, pos);
            }
        };
    }

    public static boolean wrench(EntityPlayer player, World world, BlockPos pos) {
        TileEntity tTileEntity = world.getTileEntity(pos);

        if (tTileEntity == null || player instanceof FakePlayer) {
            return player instanceof EntityPlayerMP;
        }

        if (player instanceof EntityPlayerMP && tTileEntity instanceof AlignmentProvider) {
            Alignment alignment = ((AlignmentProvider) tTileEntity).getAlignment();

            if (alignment != null) {
                if (player.isSneaking()) {
                    alignment.toolSetFlip(null);
                } else {
                    alignment.toolSetRotation(null);
                }

                return true;
            }
        }
        return false;
    }

    /**
     * This Function determines the direction a Block gets when being Wrenched. returns -1 if invalid. Even though that
     * could never happen. Normalizes values into the range [0.0f, 1.0f].
     */
    public static EnumFacing determineWrenchingSide(EnumFacing side, float aX, float aY, float aZ) {
        float modX = (aX % 1.0f + 1.0f) % 1.0f;
        float modY = (aY % 1.0f + 1.0f) % 1.0f;
        float modZ = (aZ % 1.0f + 1.0f) % 1.0f;
        EnumFacing tBack = side.getOpposite();
        // The = here is necessary; Since the hitVec only has a precision of 1/16th on MP and gets rounded down,
        // a value of 0.8 would be 0.75 on MP, which is not > 0.75, returning false.
        switch (side) {
            case DOWN, UP -> {
                if (modX < 0.25) {
                    if (modZ < 0.25) return tBack;
                    if (modZ >= 0.75) return tBack;
                    return EnumFacing.WEST;
                }
                if (modX >= 0.75) {
                    if (modZ < 0.25) return tBack;
                    if (modZ >= 0.75) return tBack;
                    return EnumFacing.EAST;
                }
                if (modZ < 0.25) return EnumFacing.NORTH;
                if (modZ >= 0.75) return EnumFacing.SOUTH;
            }
            case NORTH, SOUTH -> {
                if (modX < 0.25) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return EnumFacing.WEST;
                }
                if (modX >= 0.75) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return EnumFacing.EAST;
                }
                if (modY < 0.25) return EnumFacing.DOWN;
                if (modY >= 0.75) return EnumFacing.UP;
            }
            case WEST, EAST -> {
                if (modZ < 0.25) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return EnumFacing.NORTH;
                }
                if (modZ >= 0.75) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return EnumFacing.SOUTH;
                }
                if (modY < 0.25) return EnumFacing.DOWN;
                if (modY >= 0.75) return EnumFacing.UP;
            }
        }

        return side;
    }
}
