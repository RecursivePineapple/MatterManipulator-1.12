package matter_manipulator.compat.emc.io;

import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;

import matter_manipulator.core.context.PlayerContext;
import matter_manipulator.core.item.ItemStackIO;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.resources.ResourceIOFactory;

public class ArcaneTabletItemStackIOFactory implements ResourceIOFactory<ItemStackIO> {

    @Override
    public Optional<ItemStackIO> getIO(PlayerContext context, IDataStorage storage) {
        EntityPlayer player = context.getRealPlayer();

        var state = ArcaneTabletState.fromPlayer(player);

        return Optional.of(new ArcaneTabletItemStackIO(state));
    }
}
