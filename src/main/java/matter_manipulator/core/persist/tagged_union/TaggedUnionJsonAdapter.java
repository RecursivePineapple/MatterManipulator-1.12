package matter_manipulator.core.persist.tagged_union;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import matter_manipulator.MatterManipulator;

public class TaggedUnionJsonAdapter<Loader extends TaggedUnionLoader<Variant>, Variant extends TaggedUnionVariant<Variant>> implements JsonSerializer<Variant>, JsonDeserializer<Variant> {

    private final String objectName;
    private final Map<String, Loader> loaders;

    public TaggedUnionJsonAdapter(String objectName, Map<String, Loader> loaders) {
        this.objectName = objectName;
        this.loaders = loaders;
    }

    @Override
    public Variant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!(json instanceof JsonObject obj)) {
            MatterManipulator.LOG.error("Expected JsonObject, got {}", json, new Exception());
            return null;
        }

        for (var e : obj.entrySet()) {
            Loader loader = loaders.get(e.getKey());

            if (loader == null) {
                MatterManipulator.LOG.error("Could not load {} {}: {} loader {} was not found", objectName, e.getValue(), objectName, e.getKey(), new Exception());
                break;
            }

            return loader.load(e.getValue());
        }

        return null;
    }

    @Override
    public JsonElement serialize(Variant src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        obj.add(src.getLoader().getKey(), src.getLoader().save(src));

        return obj;
    }
}
