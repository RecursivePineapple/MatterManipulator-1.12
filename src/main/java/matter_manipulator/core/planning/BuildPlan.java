package matter_manipulator.core.planning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import matter_manipulator.MatterManipulator;
import matter_manipulator.common.state.MMState;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.core.building.Plannable;
import matter_manipulator.core.context.HeldManipulatorContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.i18n.MMTextBuilder;
import matter_manipulator.core.modes.ManipulatorMode;
import matter_manipulator.core.persist.NBTPersist;
import matter_manipulator.core.resources.ResourceIdentity;
import matter_manipulator.core.resources.ResourceIdentityTrait;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.util.Coroutine;

public class BuildPlan {

    public final Object2LongOpenHashMap<ResourceIdentity> required = new Object2LongOpenHashMap<>();
    public String name = "<unnamed plan>";
    /// Plan will be automatically submitted ASAP and removed once finished (or failed).
    public boolean autoSubmit;
    public UUID submittingPlayer = UUID.randomUUID();

    public BuildPlan() {
    }

    public BuildPlan(@NotNull List<ResourceStack> requiredResources) {
        for (var stack : requiredResources) {
            this.required.addTo(stack.getIdentity(), ResourceStack.getStackAmount(stack));
        }

        this.required.object2LongEntrySet().removeIf(e -> e.getLongValue() <= 0);
    }

    public BuildPlan(Object2LongOpenHashMap<ResourceIdentity> required) {
        required.object2LongEntrySet().fastForEach(e -> {
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

    @Nullable
    public EntityPlayerMP getPlayer(MinecraftServer server) {
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            if (player.getGameProfile().getId().equals(this.submittingPlayer)) return player;
        }

        return null;
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

    public static NBTTagCompound writeToTag(BuildPlan plan) {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setString("name", plan.name);
        tag.setLong("u1", plan.submittingPlayer.getLeastSignificantBits());
        tag.setLong("u2", plan.submittingPlayer.getMostSignificantBits());
        tag.setBoolean("auto", plan.autoSubmit);

        NBTTagList required = new NBTTagList();
        tag.setTag("required", required);

        plan.required.object2LongEntrySet().fastForEach(e -> {
            NBTTagCompound reqTag = new NBTTagCompound();
            required.appendTag(reqTag);

            reqTag.setLong("amount", e.getLongValue());

            var id = e.getKey();

            if (id.hasTrait(ResourceIdentityTrait.IntAmount)) {
                var stack = id.asInt().createStackInt(1);

                reqTag.setTag("stack", NBTPersist.toNbt(NBTPersist.GSON.toJsonTree(stack)));
            } else if (id.hasTrait(ResourceIdentityTrait.LongAmount)) {
                var stack = id.asLong().createStackLong(1);

                reqTag.setTag("stack", NBTPersist.toNbtExact(NBTPersist.GSON.toJsonTree(stack)));
            } else {
                MatterManipulator.LOG.error("Resource was skipped while saving plan: {}", id);
            }
        });

        return tag;
    }

    public static BuildPlan readFromTag(NBTTagCompound tag) {
        BuildPlan plan = new BuildPlan();

        plan.name = tag.getString("name");
        plan.submittingPlayer = new UUID(tag.getLong("u2"), tag.getLong("u1"));
        plan.autoSubmit = tag.getBoolean("auto");

        for (var reqTag : MCUtils.getCompoundTagList(tag, "required")) {
            long amount = reqTag.getLong("amount");

            ResourceStack stack = NBTPersist.GSON.fromJson(NBTPersist.toJsonObjectExact(reqTag.getCompoundTag("stack")), ResourceStack.class);

            plan.required.put(stack.getIdentity(), amount);
        }

        return plan;
    }
}
