package matter_manipulator.client.rendering.models;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.github.bsideup.jabel.Desugar;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.common.utils.enums.ExtendedFacing;

public class BakedMachineModel implements IBakedModel {

    public final IBakedModel base;
    public final VertexFormat format;
    private final IUnlistedProperty<ExtendedFacing> facingProp;

    private final int posX, posY, posZ, stride;
    // Int index of the packed normal in the vertex data, or -1 if the format has no normal.
    private final int normalOffset;

    private final UV[] uvs;

    @Desugar
    private record UV(int offset, int len) {

        public void take(int[] data, int vert, int stride, int[] dst, int dstOffset) {
            for (int i = 0; i < len; i++) {
                dst[i + dstOffset * len] = data[stride * vert + this.offset + i];
            }
        }

        public void put(int[] data, int vert, int stride, int[] dst, int dstOffset) {
            for (int i = 0; i < len; i++) {
                data[stride * vert + this.offset + i] = dst[i + dstOffset * len];
            }
        }

        public void swap(int[] data, int vert, int stride, int[] dst, int dstOffset) {
            for (int i = 0; i < len; i++) {
                int temp = dst[i + dstOffset * len];

                dst[i + dstOffset * len] = data[stride * vert + this.offset + i];

                data[stride * vert + this.offset + i] = temp;
            }
        }
    }

    public BakedMachineModel(IBakedModel base, VertexFormat format, IUnlistedProperty<ExtendedFacing> facingProp) {
        this.base = base;
        this.format = format;
        this.facingProp = facingProp;

        int offset = 0;
        VertexFormatElement element = null;

        for (var e : format.getElements()) {
            if (e.isPositionElement()) {
                element = e;
                break;
            }

            offset += e.getSize();
        }

        if (offset % 4 != 0) throw new IllegalStateException("Invalid vertex format: expected position to be int-aligned: " + format);
        if (element == null) throw new IllegalStateException("Invalid vertex format: position element was missing: " + format);
        if (element.getType() != EnumType.FLOAT) throw new IllegalStateException("Invalid vertex format: position element must be float: " + format);

        offset /= 4;

        posX = offset;
        posY = offset + 1;
        posZ = offset + 2;

        stride = format.getIntegerSize();

        int offset2 = 0;
        int normalOffset = -1;

        ArrayList<UV> uvs = new ArrayList<>();

        for (var e : format.getElements()) {
            switch (e.getUsage()) {
                case COLOR, UV -> {
                    if ((offset2 % 4) != 0) throw new IllegalStateException("Invalid vertex format: expected element to be int-aligned: " + e);

                    int len = e.getSize();

                    len = (len / 4) + (len % 4 == 0 ? 0 : 1);

                    uvs.add(new UV(offset2 / 4, len));
                }
                case NORMAL -> {
                    if ((offset2 % 4) == 0 && e.getType() == EnumType.BYTE) {
                        normalOffset = offset2 / 4;
                    }
                }
            }

            offset2 += e.getSize();
        }

        this.normalOffset = normalOffset;
        this.uvs = uvs.toArray(new UV[0]);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state == null) {
            return base.getQuads(null, side, rand);
        }

        IExtendedBlockState extended = (IExtendedBlockState) state;

        ExtendedFacing facing = extended.getValue(facingProp);

        EnumFacing realSide = toModelSide(facing, side);

        List<BakedQuad> quads = base.getQuads(state, realSide, rand);

        Vector3f temp = new Vector3f();

        int scratchLength = 0;

        for (UV uv : uvs) {
            scratchLength += uv.len;
        }

        int[] scratch = new int[scratchLength * 4];

        return DataUtils.mapToList(quads, quad -> transformQuad(quad, side, facing, temp, scratch));
    }

    // Inverse of our vertex transform: given a world-space face direction, returns the model-space
    // face direction that maps to it. Derived from integerAxisSwap.translate + sign flip to match
    // the (a=-x, b=-y, c=z) basis used in transformQuad.
    private static EnumFacing toModelSide(ExtendedFacing facing, EnumFacing worldSide) {
        if (worldSide == null) return null;
        Vector3f abc = facing.getIntegerAxisSwap().translate(MathUtils.v(worldSide));
        return MathUtils.vprime(new Vector3f(-abc.x, -abc.y, abc.z));
    }

    private BakedQuad transformQuad(BakedQuad src, EnumFacing side, ExtendedFacing facing, Vector3f temp, int[] tempInts) {
        BakedQuad out = new BakedQuad(
            src.getVertexData().clone(),
            src.getTintIndex(),
            side,
            src.getSprite(),
            src.shouldApplyDiffuseLighting(),
            src.getFormat());

        int[] data = out.getVertexData();

        int offset = 0;

        for (int i = 0; i < 4; i++) {
            // Center around 0.5, convert to ABC space (NORTH_NORMAL_NONE basis: a=-x, b=-y, c=z),
            // then inverseTranslate back to world XYZ for the target facing.
            float cx = Float.intBitsToFloat(data[offset + posX]) - 0.5f;
            float cy = Float.intBitsToFloat(data[offset + posY]) - 0.5f;
            float cz = Float.intBitsToFloat(data[offset + posZ]) - 0.5f;

            temp = facing.getIntegerAxisSwap().inverseTranslate(new Vector3f(-cx, -cy, cz));

            data[offset + posX] = Float.floatToIntBits(temp.x + 0.5f);
            data[offset + posY] = Float.floatToIntBits(temp.y + 0.5f);
            data[offset + posZ] = Float.floatToIntBits(temp.z + 0.5f);

            if (normalOffset >= 0) {
                int packed = data[offset + normalOffset];
                float nx = (byte)(packed & 0xFF) / 127.0f;
                float ny = (byte)((packed >> 8) & 0xFF) / 127.0f;
                float nz = (byte)((packed >> 16) & 0xFF) / 127.0f;

                temp = facing.getIntegerAxisSwap().inverseTranslate(new Vector3f(-nx, -ny, nz));

                data[offset + normalOffset] = ((byte)(temp.x * 127) & 0xFF)
                    | (((byte)(temp.y * 127) & 0xFF) << 8)
                    | (((byte)(temp.z * 127) & 0xFF) << 16)
                    | (packed & 0xFF000000);
            }

            offset += stride;
        }

        // A single flip is a reflection, which reverses winding order.
        // BOTH is two reflections, so winding is preserved.
        if (facing.getFlip().isEitherFlipped()) {
            swap(data, stride, 3 * stride, stride);
        }

        return out;
    }

    private static void swap(int[] data, int a, int b, int len) {
        for(int i = 0; i < len; i++) {
            int x = data[a + i];
            data[a + i] = data[b + i];
            data[b + i] = x;
        }
    }

    @Override
    public boolean isAmbientOcclusion() {
        return base.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return base.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return base.getParticleTexture();
    }

    @Override
    public @NotNull ItemOverrideList getOverrides() {
        return base.getOverrides();
    }

}
