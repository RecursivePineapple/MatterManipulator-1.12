package matter_manipulator.common.uplink;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import matter_manipulator.core.resources.ResourceStack;

public interface UplinkPatternReceiver {

    void createPlan(EntityPlayer submitter, String name, List<ResourceStack> requirements, boolean autoSubmit);

    int getAutoPlanCount();
    int getManualPlanCount();

}
