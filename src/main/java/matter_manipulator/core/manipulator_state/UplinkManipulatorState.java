package matter_manipulator.core.manipulator_state;

import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.uplink.Uplink;
import matter_manipulator.core.persist.StateSandbox;

public class UplinkManipulatorState implements ManipulatorState {

    private final StateSandbox state;

    private static class Address {
        public long addr;

        public Address() {
        }

        public Address(long addr) {
            this.addr = addr;
        }
    }

    public UplinkManipulatorState(StateSandbox state) {
        this.state = state;
    }

    public long getUplinkAddress() {
        Address addr = state.load(Address.class);

        return addr == null ? 0 : addr.addr;
    }

    public void setUplinkAddress(long address) {
        state.save(new Address(address));
    }

    @Nullable
    public Uplink getUplink() {
        Address addr = state.load(Address.class);

        if (addr == null) return null;

        return Uplink.getUplink(addr.addr);
    }

    public boolean hasPlanReceiver() {
        Uplink uplink = getUplink();

        if (uplink == null) return false;

        return uplink.getPlanReceiver() != null;
    }
}
