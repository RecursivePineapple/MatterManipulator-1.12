package matter_manipulator.compat.probe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import matter_manipulator.common.uplink.TileUplinkController;
import matter_manipulator.common.uplink.TileUplinkModule;
import matter_manipulator.common.uplink.UplinkPlanReceiver;
import matter_manipulator.common.uplink.UplinkPowerProvider;
import matter_manipulator.common.utils.MCUtils;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.api.ProbeMode;

public class UplinkModuleInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return "matter-manipulator:uplink-module-provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, EntityPlayer entityPlayer, World world, IBlockState state, IProbeHitData hitData) {
        if (world.getTileEntity(hitData.getPos()) instanceof TileUplinkController controller) {
            probeInfo.text("Address: " + Long.toHexString(controller.getAddress()));
        }

        if (world.getTileEntity(hitData.getPos()) instanceof TileUplinkModule module) {
            if (module instanceof UplinkPowerProvider power) {
                long stored = power.getStoredEnergy();
                long max = power.getMaxStoredEnergy();
                probeInfo.progress(stored, max, probeInfo.defaultProgressStyle().width(175).suffix(" / " + MCUtils.formatNumbers(max) + " RF").filledColor(0xFFEE0007).alternateFilledColor(0xFFEE0007).borderColor(0xff555555).numberFormat(NumberFormat.COMMAS));
            }

            if (module instanceof UplinkPlanReceiver pattern) {
                var plans = pattern.getPlans();

                probeInfo.text("Auto Plans: " + plans.stream().filter(p -> p.autoSubmit).count());
                probeInfo.text("Manual Plans: " + plans.stream().filter(p -> !p.autoSubmit).count());
            }

            probeInfo.text(module.isConnected() ? "Connected" : "Not Connected");
            probeInfo.text(module.isActive() ? "Active" : "Not Active");
        }
    }
}
