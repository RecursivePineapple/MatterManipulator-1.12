package matter_manipulator.core.block_spec;

import matter_manipulator.core.persist.tagged_union.TaggedUnionLoader;

/// Something that can load a block spec from json and vice versa. The external storage format for specs can be thought
/// of as a tagged union. The format looks something like the following.
/// ```json
/// {
///   // not controlled within this system - this may be anything as loaders are invoked via a JsonSerializer/JsonDeserializer
///   "cables": {
///     // from getKey()
///     "block": {
///       // the generic data this loader interacts with
///       "state": "minecraft:stairs{facing=down,half=bottom,shape=straight}"
///     }
///   }
/// }
/// ```
public interface BlockSpecLoader extends TaggedUnionLoader<BlockSpec> {
}
