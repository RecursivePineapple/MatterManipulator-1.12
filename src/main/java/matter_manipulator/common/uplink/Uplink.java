package matter_manipulator.common.uplink;

import java.lang.ref.WeakReference;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import matter_manipulator.core.resources.ResourceStack;

public interface Uplink {

    Long2ObjectOpenHashMap<WeakReference<Uplink>> UPLINKS = new Long2ObjectOpenHashMap<>();

    static Uplink getUplink(long address) {
        var ref = UPLINKS.get(address);

        return ref == null ? null : ref.get();
    }

    long drainEnergy(long request);
    void createPlan(EntityPlayer submitter, String name, List<ResourceStack> requirements, boolean autoSubmit);

}
