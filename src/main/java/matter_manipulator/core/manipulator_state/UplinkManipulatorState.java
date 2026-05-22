package matter_manipulator.core.manipulator_state;

import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.uplink.Uplink;
import matter_manipulator.core.persist.StateSandbox;

public class UplinkManipulatorState implements ManipulatorState {

    private final StateSandbox state;

    public UplinkManipulatorState(StateSandbox state) {
        this.state = state;
    }

    public long getUplinkAddress() {
        Long addr = state.load(Long.class);
        return addr == null ? 0 : addr;
    }

    public void setUplinkAddress(long address) {
        state.save(address);
    }

    @Nullable
    public Uplink getUplink() {
        Long addr = state.load(Long.class);

        if (addr == null) return null;

        return Uplink.getUplink(addr);
    }
}
