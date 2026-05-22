package matter_manipulator.common.uplink;

public interface UplinkPowerProvider {

    long getMaxStoredEnergy();
    long getStoredEnergy();
    long drainEnergy(long request);

}
