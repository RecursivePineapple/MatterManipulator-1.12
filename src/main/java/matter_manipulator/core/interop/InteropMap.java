package matter_manipulator.core.interop;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.context.AnalysisContext;
import matter_manipulator.core.context.ManipulatorPlacingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.persist.DataStorage;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.persist.NBTPersist;
import matter_manipulator.core.resources.ResourceIdentity;
import matter_manipulator.core.resources.ResourceStack;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class InteropMap extends Object2ObjectOpenHashMap<InteropModule, Object> implements Cloneable {

    public void transform(Transform transform) {
        //noinspection unchecked
        this.replaceAll((module, state) -> module.transform(state, transform));
    }

    public List<Localized> getDetails() {
        List<Localized> details = new ArrayList<>();

        for (var e : this.object2ObjectEntrySet()) {
            //noinspection unchecked
            e.getKey().getDetails(details, e.getValue());
        }

        return details;
    }

    public EnumSet<ApplyResult> apply(ManipulatorPlacingContext context) {
        EnumSet<ApplyResult> result = EnumSet.noneOf(ApplyResult.class);

        for (var e : this.object2ObjectEntrySet()) {
            //noinspection unchecked
            result.addAll(e.getKey().apply(context, e.getValue()));
        }

        return result;
    }

    public void saveInterop(JsonObject specRoot) {
        DataStorage storage = new DataStorage();

        for (var e : this.object2ObjectEntrySet()) {
            //noinspection unchecked
            e.getKey().save(storage, e.getValue());
        }

        if (storage.state.size() > 0) {
            specRoot.add("interop", NBTPersist.GSON.toJsonTree(storage, IDataStorage.class));
        }
    }

    public void loadInterop(JsonObject specRoot) {
        this.clear();

        if (specRoot.has("interop")) {
            DataStorage storage = NBTPersist.GSON.fromJson(specRoot.get("interop"), DataStorage.class);

            //noinspection rawtypes
            for (InteropModule interop : MMRegistriesInternal.INTEROP_MODULES.sorted()) {
                @SuppressWarnings("rawtypes")
                Optional result = interop.load(storage);

                if (result.isPresent()) {
                    this.put(interop, result.get());
                }
            }
        }
    }

    public void analyze(AnalysisContext context) {
        this.clear();

        for (InteropModule<?> interop : MMRegistriesInternal.INTEROP_MODULES.sorted()) {
            var result = interop.analyze(context);

            if (!result.isPresent()) continue;

            this.put(interop, result.get());
        }
    }

    public void modifyResource(ResourceStack resource) {
        for (var e : this.object2ObjectEntrySet()) {
            //noinspection unchecked
            e.getKey().modifyResource(resource, e.getValue());
        }
    }

    public void exchange(ResourceIdentity stack, ResourceIdentity replacement) {
        for (var e : this.object2ObjectEntrySet()) {
            e.getKey().exchangeResource(e.getValue(), stack, replacement);
        }
    }

    public void putAllCopied(Map<InteropModule, Object> src) {
        src.forEach((module, analysis) -> {
            this.put(module, module.cloneAnalysis(analysis));
        });
    }

    public void getRequiredResources(ManipulatorPlacingContext context, boolean skipExisting) {
        for (var e : this.object2ObjectEntrySet()) {
            if (skipExisting) {
                //noinspection unchecked
                e.getKey().getRequiredResourcesForNewBlock(context, e.getValue());
            } else {
                //noinspection unchecked
                e.getKey().getRequiredResourcesForExistingBlock(context, e.getValue());
            }
        }
    }

    @Override
    public InteropMap clone() {
        var map = (InteropMap) super.clone();

        //noinspection unchecked
        map.replaceAll(InteropModule::cloneAnalysis);

        return map;
    }
}
