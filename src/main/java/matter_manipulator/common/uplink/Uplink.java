package matter_manipulator.common.uplink;

import java.lang.ref.WeakReference;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public interface Uplink {

    Long2ObjectOpenHashMap<WeakReference<Uplink>> UPLINKS = new Long2ObjectOpenHashMap<>();

    static Uplink getUplink(long address) {
        var ref = UPLINKS.get(address);

        return ref == null ? null : ref.get();
    }

    long drainEnergy(long request);

    @Nullable
    UplinkPlanReceiver getPlanReceiver();
}
