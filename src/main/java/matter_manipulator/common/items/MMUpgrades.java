package matter_manipulator.common.items;

import net.minecraft.item.ItemStack;

import matter_manipulator.CommonProxy;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import matter_manipulator.core.util.FlagSet;

public enum MMUpgrades {

    PowerP2P(IDMetaItem.UpgradePowerP2P, 0, FlagSet.of()),
    Mining(IDMetaItem.UpgradePrototypeMining, 1, FlagSet.of(ManipulatorFlags.ALLOW_REMOVING)),
    Speed(IDMetaItem.UpgradeSpeed, 2, FlagSet.of()),
    PowerEff(IDMetaItem.UpgradePowerEff, 3, FlagSet.of()),
    //
    ;

    public static final Int2ObjectMap<MMUpgrades> UPGRADES_BY_META = new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<MMUpgrades> UPGRADES_BY_BIT = new Int2ObjectOpenHashMap<>();

    static {
        for (MMUpgrades upgrade : MMUpgrades.values()) {
            UPGRADES_BY_META.put(upgrade.id, upgrade);
            UPGRADES_BY_BIT.put(upgrade.bit, upgrade);
        }
    }

    public final int id;
    public final int bit;
    public final FlagSet providesCaps;

    MMUpgrades(IDMetaItem id, int bit, FlagSet providesCaps) {
        this.id = id.ID;
        this.bit = bit;
        this.providesCaps = providesCaps;
    }

    public ItemStack getStack() {
        return new ItemStack(CommonProxy.META_ITEM, 1, id);
    }
}
