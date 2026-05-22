package matter_manipulator.common.uplink;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import org.jetbrains.annotations.Nullable;

import mcp.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TileUplinkEnergyConnector extends TileUplinkModule implements UplinkPowerProvider, IEnergyStorage {

    public int stored;

    @Override
    public @Nullable <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (facing == getFrontFacing()) {
            if (capability == CapabilityEnergy.ENERGY) {
                return CapabilityEnergy.ENERGY.cast(this);
            }
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        this.stored = compound.getInteger("energy");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setInteger("energy", stored);

        return compound;
    }

    @Override
    public long getMaxStoredEnergy() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getStoredEnergy() {
        return stored;
    }

    @Override
    public long drainEnergy(long request) {
        long drainable = Math.min(stored, request);

        stored -= drainable;

        return drainable;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyReceived = Math.min(Integer.MAX_VALUE - stored, maxReceive);

        if (!simulate) {
            stored += energyReceived;
        }

        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(stored, maxExtract);

        if (!simulate) {
            stored -= energyExtracted;
        }

        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return stored;
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
