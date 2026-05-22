package matter_manipulator.common.modes.exchanging;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import lombok.EqualsAndHashCode;
import matter_manipulator.client.rendering.MMRenderConstants;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.common.utils.math.Location;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.context.HeldManipulatorContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.IntItemResourceStack;

@EqualsAndHashCode
public class ExchangeConfig {

    public PendingAction action;
    public Location a, b;

    public boolean advancedMode;

    public List<Exchange> exchanges = new ArrayList<>();

    @EqualsAndHashCode
    public static class Exchange {
        public ResourceStack target = IntItemResourceStack.EMPTY;
        public ResourceStack replacement = IntItemResourceStack.EMPTY;

        public boolean isValid() {
            return target != null && replacement != null;
        }

        public ResourceStack target() {
            return target == null ? IntItemResourceStack.EMPTY : target;
        }

        public ResourceStack replacement() {
            return replacement == null ? IntItemResourceStack.EMPTY : replacement;
        }
    }

    public enum PendingAction {
        MARK_A {
            @Override
            public Optional<ExchangeConfig> process(ExchangeConfig config, HeldManipulatorContext context,
                boolean forPreview) {
                config.a = context.getLookedAtBlock();

                if (!forPreview) {
                    config.action = PendingAction.MARK_B;
                }

                return Optional.of(config);
            }

            @Override
            public ImmutableColor getRulerColor() {
                return MMRenderConstants.BLUE;
            }
        },
        MARK_B {
            @Override
            public Optional<ExchangeConfig> process(ExchangeConfig config, HeldManipulatorContext context,
                boolean forPreview) {
                config.b = context.getLookedAtBlock();

                if (!forPreview) {
                    config.action = null;
                }

                return Optional.of(config);
            }

            @Override
            public ImmutableColor getRulerColor() {
                return MMRenderConstants.BLUE;
            }
        },
        SET_TARGET {
            @Override
            public Optional<ExchangeConfig> process(ExchangeConfig config, HeldManipulatorContext context,
                boolean forPreview) {

                BlockSpec selected = null;

                var hit = context.getHitResult();

                if (hit != null) {
                    selected = MMRegistriesInternal.getFullBlockSpec(context, hit.getBlockPos());
                }

                if (selected == null) {
                    selected = BlockSpec.air();
                }

                config.exchanges.removeIf(e -> !e.isValid());

                if (config.exchanges.isEmpty()) {
                    config.exchanges.add(new Exchange());
                } else if (config.exchanges.size() > 1) {
                    config.exchanges = new ArrayList<>(config.exchanges.subList(0, 1));
                }

                config.exchanges.get(0).target = selected.getResource();

                if (!forPreview) {
                    if (context.isRemote()) {
                        MCUtils.sendInfoToPlayer(context.getRealPlayer(),
                            new Localized(
                                "mm.info.exch.replacing",
                                config.exchanges.get(0).target().getName(),
                                config.exchanges.get(0).replacement().getName()));
                    }

                    config.action = null;
                }

                return Optional.of(config);
            }

            @Override
            public ImmutableColor getRulerColor() {
                return MMRenderConstants.BLUE;
            }
        },
        SET_REPLACEMENT {
            @Override
            public Optional<ExchangeConfig> process(ExchangeConfig config, HeldManipulatorContext context,
                boolean forPreview) {

                BlockSpec selected = null;

                var hit = context.getHitResult();

                if (hit != null) {
                    selected = MMRegistriesInternal.getFullBlockSpec(context, hit.getBlockPos());
                }

                if (selected == null) {
                    selected = BlockSpec.air();
                }

                config.exchanges.removeIf(e -> !e.isValid());

                if (config.exchanges.isEmpty()) {
                    config.exchanges.add(new Exchange());
                } else if (config.exchanges.size() > 1) {
                    config.exchanges = new ArrayList<>(config.exchanges.subList(0, 1));
                }

                config.exchanges.get(0).replacement = selected.getResource();

                if (!forPreview) {
                    if (context.isRemote()) {
                        MCUtils.sendInfoToPlayer(context.getRealPlayer(),
                            new Localized(
                                "mm.info.exch.replacing",
                                config.exchanges.get(0).target().getName(),
                                config.exchanges.get(0).replacement().getName()));
                    }

                    config.action = null;
                }

                return Optional.of(config);
            }

            @Override
            public ImmutableColor getRulerColor() {
                return MMRenderConstants.ORANGE;
            }
        },
        //
        ;

        public Optional<ExchangeConfig> process(ExchangeConfig config, HeldManipulatorContext context, boolean forPreview) {
            throw new UnsupportedOperationException();
        }

        /// Gets the ruler color, or null if rulers should not be shown.
        @Nullable
        public ImmutableColor getRulerColor() {
            return null;
        }
    }

}
