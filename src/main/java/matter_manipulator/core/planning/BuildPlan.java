package matter_manipulator.core.planning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import matter_manipulator.common.state.MMState;
import matter_manipulator.core.building.Plannable;
import matter_manipulator.core.context.HeldManipulatorContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.i18n.MMTextBuilder;
import matter_manipulator.core.modes.ManipulatorMode;
import matter_manipulator.core.resources.ResourceIdentity;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.util.Coroutine;

public class BuildPlan {

    public final Object2LongOpenHashMap<ResourceIdentity> required = new Object2LongOpenHashMap<>();

    public BuildPlan(@NotNull List<ResourceStack> requiredResources) {
        for (var stack : requiredResources) {
            this.required.addTo(stack.getIdentity(), ResourceStack.getStackAmount(stack));
        }

        this.required.object2LongEntrySet().removeIf(e -> e.getLongValue() <= 0);
    }

    public BuildPlan(Object2LongOpenHashMap<ResourceIdentity> required) {
        required.object2LongEntrySet().forEach(e -> {
            if (e.getLongValue() > 0) {
                this.required.put(e.getKey(), e.getLongValue());
            }
        });
    }

    public List<Localized> toMessage() {
        List<Localized> out = new ArrayList<>();

        switch (required.size()) {
            case 0 -> {
                out.add(new Localized("mm.chat.plan-header.empty").setBase(TextFormatting.GRAY));
            }
            case 1 -> {
                out.add(new Localized("mm.chat.plan-header").setBase(TextFormatting.GRAY));
            }
            default -> {
                out.add(new Localized("mm.chat.plan-header.plural").setBase(TextFormatting.GRAY));
            }
        }

        this.required.object2LongEntrySet()
            .stream()
            .sorted(Comparator.comparingLong(Entry::getLongValue))
            .forEach(e -> {
                out.add(new MMTextBuilder("mm.chat.plan-entry").setBase(TextFormatting.GRAY)
                    .addLocalized(e.getKey().getName())
                    .addNumber(e.getLongValue())
                    .toLocalized());
            });

        return out;
    }

    public void sendChatToPlayer(EntityPlayer player) {
        for (var msg : toMessage()) {
            msg.sendChat(player);
        }
    }

    public static Coroutine<BuildPlan> createPlan(HeldManipulatorContext context, boolean skipExisting) {
        MMState state = context.getState();

        @SuppressWarnings("rawtypes")
        ManipulatorMode mode = state.getActiveMode();

        if (mode == null) return Coroutine.finished(new BuildPlan(Collections.emptyList()));

        Object config = mode.loadConfig(state.getActiveModeConfigStorage());

        //noinspection unchecked
        return mode.startAnalysis(config, context).then((Object build) -> {
            if (build instanceof Plannable plannable) {
                return plannable.createPlan(context, skipExisting).then(plan -> {
                    plan.sendChatToPlayer(context.getRealPlayer());
                    return Coroutine.finished(plan);
                });
            } else {
                new Localized("mm.chat.not-plannable").sendChat(context.getRealPlayer());
                return Coroutine.finished(new BuildPlan(Collections.emptyList()));
            }
        });
    }
}
