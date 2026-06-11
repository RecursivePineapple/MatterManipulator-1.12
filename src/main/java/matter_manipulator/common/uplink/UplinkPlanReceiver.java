package matter_manipulator.common.uplink;

import java.util.List;

import matter_manipulator.core.planning.BuildPlan;

public interface UplinkPlanReceiver {

    void addPlan(BuildPlan plan);
    List<BuildPlan> getPlans();
    void deletePlan(BuildPlan plan);
}
