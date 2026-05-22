package matter_manipulator.common.modes.exchanging;

import static matter_manipulator.common.utils.MCUtils.BLUE;
import static matter_manipulator.common.utils.MCUtils.GRAY;
import static matter_manipulator.common.utils.MCUtils.processFormatStacks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Contract;

import com.cleanroommc.modularui.api.drawable.IKey;
import it.unimi.dsi.fastutil.Pair;
import matter_manipulator.GlobalMMConfig.DebugConfig;
import matter_manipulator.MatterManipulator;
import matter_manipulator.Tags;
import matter_manipulator.client.gui.BranchableRadialMenu;
import matter_manipulator.client.rendering.ModeRenderer;
import matter_manipulator.client.rendering.modes.ExchangeModeRenderer;
import matter_manipulator.common.block_spec.BlockSpecData;
import matter_manipulator.common.building.StandardBuild;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.items.ManipulatorFlags;
import matter_manipulator.common.modes.ResettableMode;
import matter_manipulator.common.modes.exchanging.ExchangeConfig.PendingAction;
import matter_manipulator.common.networking.MMPacketBuffer;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.common.utils.math.Location;
import matter_manipulator.core.analysis.BlockAnalyzer;
import matter_manipulator.core.block_spec.BlockSpec;
import matter_manipulator.core.building.PendingBlock;
import matter_manipulator.core.context.HeldManipulatorContext;
import matter_manipulator.core.context.RenderingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.modes.ManipulatorMode;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.resources.ResourceIdentity;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.util.Coroutine;

public class ExchangeManipulatorMode implements ManipulatorMode<ExchangeConfig, StandardBuild>, ResettableMode {

    public static final ResourceLocation EXCHANGE = new ResourceLocation(Tags.MODID, "exchange");

    @Override
    public ResourceLocation getModeID() {
        return EXCHANGE;
    }

    @Override
    public String getLocalizedName() {
        return "Exchange";
    }

    @Override
    public boolean isAllowedOnManipulator(HeldManipulatorContext context) {
        return context.hasCapability(ManipulatorFlags.ALLOW_EXCHANGING);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModeRenderer<ExchangeConfig, StandardBuild> getRenderer(RenderingContext context) {
        return new ExchangeModeRenderer();
    }

    @Override
    public ExchangeConfig getPreviewConfig(ExchangeConfig config, HeldManipulatorContext context) {
        if (config.action != null) {
            Optional<ExchangeConfig> result = config.action.process(config, context, true);

            if (result.isPresent()) {
                return result.get();
            }
        }

        return config;
    }

    @Override
    public void addTooltipInfo(HeldManipulatorContext context, List<String> lines) {
        ExchangeConfig config = loadConfig(context.getState().getActiveModeConfigStorage());

        addTooltipLine(lines, "Action: ", config.action);

        addTooltipLine(lines, "A: ", config.a);
        addTooltipLine(lines, "B: ", config.b);

        addTooltipLine(lines, "Advanced Mode: ", config.advancedMode ? "True" : "False");

        lines.add(GRAY + "Replacement Rules:");
        if (config.exchanges.isEmpty()) {
            lines.add(GRAY + "None");
        } else {
            for (var rule : config.exchanges) {
                lines.add(GRAY + MCUtils.translate("mm.info.exch.replace", rule.target().getName(), rule.replacement().getName()));
            }
        }
    }

    private static void addTooltipLine(List<String> lines, String name, Object value) {
        lines.add(GRAY + name + processFormatStacks(BLUE + (value == null ? MCUtils.translate("mm.info.none") : value.toString())));
    }

    @Override
    public void addMenuItems(HeldManipulatorContext context, BranchableRadialMenu menu) {
        ExchangeConfig config = loadConfig(context.getState()
            .getActiveModeConfigStorage());

        menu.option()
            .label(IKey.str(config.advancedMode ? "Disable Advanced Mode" : "Enable Advanced Mode"))
            .onClicked(() -> {
                context.mutateConfig(this, c -> {
                    c.advancedMode ^= true;
                });
            })
            .done();

        if (config.advancedMode) {
            menu.option()
                .label(IKey.str("Edit Rules"))
                .onClicked(() -> {

                })
                .done();
        } else {
            // spotless:off
            menu.branch()
                .label(IKey.str("Select Blocks"))
                .option()
                    .label(IKey.str("Set Target"))
                    .onClicked(() -> {
                        context.mutateConfig(this, c -> {
                            c.action = PendingAction.SET_TARGET;
                        });
                    })
                .done()
                .option()
                    .label(IKey.str("Set Replacement"))
                    .onClicked(() -> {
                        context.mutateConfig(this, c -> {
                            c.action = PendingAction.SET_REPLACEMENT;
                        });
                    })
                .done()
                .done();
            // spotless:on
        }
    }

    @Override
    public Optional<ExchangeConfig> onPickBlock(ExchangeConfig config, HeldManipulatorContext context) {
        if (!config.advancedMode) {
            MCUtils.sendInfoToPlayer(context.getRealPlayer(), new Localized("mm.info.exch.no-block-pick"));
            return Optional.empty();
        }

        if (context.getRealPlayer().isSneaking()) {
            return PendingAction.SET_REPLACEMENT.process(config, context, false);
        } else {
            return PendingAction.SET_TARGET.process(config, context, false);
        }
    }

    @Override
    public Optional<ExchangeConfig> onRightClick(ExchangeConfig config, HeldManipulatorContext context) {
        if (config.action != null) {
            Optional<ExchangeConfig> result = config.action.process(config, context, false);

            if (result.isPresent()) {
                return result;
            }
        } else {
            var hit = context.getHitResult();

            if (hit != null) {
                config.action = PendingAction.MARK_A;
                config.a = null;
                config.b = null;

                return config.action.process(config, context, false);
            }
        }

        return Optional.empty();
    }

    @Override
    public ExchangeConfig loadConfig(IDataStorage storage) {
        return storage.getSandbox(getModeID()).load(ExchangeConfig.class);
    }

    @Override
    public void saveConfig(IDataStorage storage, ExchangeConfig config) {
        storage.getSandbox(getModeID()).save(config);
    }

    @Override
    public boolean needsSync() {
        return false;
    }

    @Override
    public void write(MMPacketBuffer buffer, StandardBuild buildable) {

    }

    @Override
    public StandardBuild read(MMPacketBuffer buffer) {
        return null;
    }

    @Contract(mutates = "param2")
    @Override
    public Coroutine<StandardBuild> startAnalysis(ExchangeConfig config, HeldManipulatorContext context) {
        if (!Location.areCompatible(config.a, config.b)) {
            return ctx -> ctx.stop(new StandardBuild(new ArrayDeque<>()));
        }

        return BlockAnalyzer.analyzeRegion(context, config.a, config.b)
            .then(analysis -> {
                Iterator<PendingBlock> input = analysis.blocks().iterator();

                long pre = DebugConfig.debug ? System.nanoTime() : 0;

                config.exchanges.removeIf(e -> !e.isValid());

                ArrayList<Pair<ResourceIdentity, ResourceIdentity>> exchanges = new ArrayList<>();

                for (var rule : config.exchanges) {
                    exchanges.add(Pair.of(rule.target().getIdentity(), rule.replacement().getIdentity()));
                }

                @SuppressWarnings("unchecked")
                Pair<ResourceIdentity, ResourceIdentity>[] exchangeArray = exchanges.toArray(new Pair[0]);

                return ctx -> {
                    int i = 0;

                    while (input.hasNext()) {
                        if (i++ % 10 == 0 && ctx.shouldYield()) return;

                        var block = input.next();

                        for (var op : exchangeArray) {
                            BlockSpecData result = block.spec.exchange(op.left(), op.right());

                            if (result != null) {
                                BlockSpec transformed = MMRegistriesInternal.reconstructSpec(result);

                                if (transformed != null) {
                                    block.spec = transformed;
                                }
                            }
                        }

                        // Offset to the correct location since analysis blocks are relative to the first coord
                        block.add(config.a);
                    }

                    if (DebugConfig.debug) {
                        long post = System.nanoTime();

                        MatterManipulator.LOG.info("Exchange analysis took {} ms", (post - pre) / 1e6);
                    }

                    ctx.stop(new StandardBuild(new ArrayDeque<>(analysis.blocks())));
                };
            });
    }

    private static void doExchange(PendingBlock block, ResourceStack target, ResourceStack replacement) {
        if (block.spec.getResource().isSameType(target)) {

        }
    }

    @Override
    public boolean onResetPressed(HeldManipulatorContext context) {
        context.mutateConfig(this, config -> {
            config.action = null;
            config.a = null;
            config.b = null;
        });

        return true;
    }
}
