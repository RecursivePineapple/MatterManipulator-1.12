package matter_manipulator.common.items;

import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import matter_manipulator.GlobalMMConfig;
import matter_manipulator.core.meta.MetaKey;
import matter_manipulator.core.meta.MetaMap;
import matter_manipulator.core.meta.MetadataContainer;
import matter_manipulator.core.util.FlagSet;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public enum ManipulatorTier implements MetadataContainer {

    // spotless:off
    MK0(
        OptionalInt.of(32),
        16,
        20,
        FlagSet.of(ManipulatorFlags.ALLOW_GEOMETRY),
        ImmutableList.of(MMUpgrades.Mining, MMUpgrades.Speed, MMUpgrades.PowerEff),
        MMItemList.MK0),
    MK1(
        OptionalInt.of(64),
        32,
        10,
        FlagSet.of(
            ManipulatorFlags.ALLOW_GEOMETRY,
            ManipulatorFlags.CONNECTS_TO_AE,
            ManipulatorFlags.ALLOW_REMOVING,
            ManipulatorFlags.ALLOW_EXCHANGING,
            ManipulatorFlags.ALLOW_CONFIGURING,
            ManipulatorFlags.ALLOW_CABLES),
        ImmutableList.of(MMUpgrades.Speed, MMUpgrades.PowerEff),
        MMItemList.MK1),
    MK2(
        OptionalInt.of(128),
        64,
        5,
        FlagSet.of(
            ManipulatorFlags.ALLOW_GEOMETRY,
            ManipulatorFlags.CONNECTS_TO_AE,
            ManipulatorFlags.ALLOW_REMOVING,
            ManipulatorFlags.ALLOW_EXCHANGING,
            ManipulatorFlags.ALLOW_CONFIGURING,
            ManipulatorFlags.ALLOW_CABLES,
            ManipulatorFlags.ALLOW_COPYING,
            ManipulatorFlags.ALLOW_MOVING),
        ImmutableList.of(MMUpgrades.Speed, MMUpgrades.PowerEff),
        MMItemList.MK2),
    MK3(
        OptionalInt.empty(),
        GlobalMMConfig.BuildingConfig.mk3BlocksPerPlace,
        5,
        FlagSet.of(
            ManipulatorFlags.ALLOW_GEOMETRY,
            ManipulatorFlags.CONNECTS_TO_AE,
            ManipulatorFlags.ALLOW_REMOVING,
            ManipulatorFlags.ALLOW_EXCHANGING,
            ManipulatorFlags.ALLOW_CONFIGURING,
            ManipulatorFlags.ALLOW_CABLES,
            ManipulatorFlags.ALLOW_COPYING,
            ManipulatorFlags.ALLOW_MOVING,
            ManipulatorFlags.CONNECTS_TO_UPLINK),
        ImmutableList.of(MMUpgrades.PowerEff, MMUpgrades.PowerP2P),
        MMItemList.MK3);
    // spotless:on

    public final OptionalInt maxRange;
    public final int placeSpeed, placeTicks;
    public final FlagSet capabilities;
    public final Set<MMUpgrades> allowedUpgrades;
    public final MMItemList container;

    private final MetaMap meta = new MetaMap();

    ManipulatorTier(OptionalInt maxRange, int placeSpeed, int placeTicks, FlagSet capabilities, List<MMUpgrades> allowedUpgrades, MMItemList container) {
        this.maxRange = maxRange;
        this.placeSpeed = placeSpeed;
        this.placeTicks = placeTicks;
        this.capabilities = capabilities;
        this.allowedUpgrades = Collections.unmodifiableSet(new ObjectOpenHashSet<>(allowedUpgrades));
        this.container = container;
    }

    @Nullable
    @Override
    public <T> T getMetaValue(MetaKey<T> key) {
        return meta.getMetaValue(key);
    }

    @Nullable
    @Override
    public <T> T getRequiredMetaValue(MetaKey<T> key) {
        return meta.getRequiredMetaValue(key);
    }

    @Override
    public boolean containsMetaValue(MetaKey<?> key) {
        return meta.containsMetaValue(key);
    }

    @Override
    public <T> T removeMetaValue(MetaKey<T> key) {
        return meta.removeMetaValue(key);
    }

    @Override
    public <T> void putMetaValue(MetaKey<T> key, T value) {
        meta.putMetaValue(key, value);
    }
}
