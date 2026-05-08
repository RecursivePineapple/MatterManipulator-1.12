package matter_manipulator.compat.ae2uel.io;

import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;

import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.item.ItemStackIO;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.resources.ResourceIOFactory;

public class WirelessTerminalItemStackIOFactory implements ResourceIOFactory<ItemStackIO> {

    @Override
    public Optional<ItemStackIO> getIO(ManipulatorContext context, IDataStorage storage) {
        EntityPlayer player = context.getRealPlayer();

        WirelessTerminalState wirelessTerminalState = WirelessTerminalState.getWirelessTerminal(player);

        if (wirelessTerminalState == null) return Optional.empty();

        return Optional.of(new WirelessTerminalItemStackIO(wirelessTerminalState));
    }
}
