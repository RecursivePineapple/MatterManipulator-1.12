package matter_manipulator.core.persist.tagged_union;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonElement;
import matter_manipulator.core.block_spec.BlockSpec;

/// Something that can load a tagged union variant value from json and vice versa.
/// The following blurb is an example of the json layout for [BlockSpec]s.
/// ```json
/// {
///   // `cables` isn't controlled within this system - this may be anything as loaders are invoked via a JsonSerializer/JsonDeserializer
///   "cables": {
///     // `block` is from getKey()
///     "block": {
///       // some generic data this loader interacts with
///       "state": "minecraft:stairs{facing=down,half=bottom,shape=straight}"
///     }
///   }
/// }
/// ```
public interface TaggedUnionLoader<Variant extends TaggedUnionVariant<Variant>> {

    String getKey();

    @NotNull Variant load(@NotNull JsonElement element);

    @NotNull
    JsonElement save(@NotNull Variant variant);
}
