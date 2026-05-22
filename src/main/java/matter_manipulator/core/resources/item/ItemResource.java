package matter_manipulator.core.resources.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import matter_manipulator.core.item.BigItemStack;
import matter_manipulator.core.item.ItemId;
import matter_manipulator.core.persist.NBTPersist;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.ResourceTrait;

public class ItemResource implements Resource<ResourceProvider<IntItemResourceStack>> {

    public static final ItemResource ITEMS = new ItemResource();

    private ItemResource() { }

    @Override
    public String getKey() {
        return "core:item";
    }

    @Override
    public @NotNull ResourceStack load(@NotNull JsonElement element) {
        if (!(element instanceof JsonObject obj)) throw new JsonParseException("Expected json object: " + element);

        Item item = Item.REGISTRY.getObject(new ResourceLocation(obj.get("id").getAsString()));

        if (item == null) return IntItemResourceStack.EMPTY;

        int meta = obj.get("meta").getAsInt();

        boolean isLong = false;
        long amount;

        if (obj.has("amountLong")) {
            isLong = true;
            amount = obj.get("amountLong").getAsLong();
        } else {
            amount = obj.get("amountInt").getAsInt();
        }

        NBTTagCompound tag = null, cap = null;

        if (obj.has("tag")) {
            tag = (NBTTagCompound) NBTPersist.toNbtExact(obj.get("tag"));
        }

        if (obj.has("cap")) {
            cap = (NBTTagCompound) NBTPersist.toNbtExact(obj.get("cap"));
        }

        if (isLong) {
            return new BigItemStack(ItemId.create(item, meta, tag, cap), amount);
        } else {
            ItemStack stack = new ItemStack(item, (int) amount, meta, cap);
            stack.setTagCompound(tag);
            return new ItemStackWrapper(stack);
        }
    }

    @Override
    public @NotNull JsonElement save(@NotNull ResourceStack stack) {
        if (!(stack instanceof ItemResourceStack item)) throw new IllegalStateException("Cannot use ItemResource to save " + stack);

        JsonObject obj = new JsonObject();

        obj.addProperty("id", item.getItem().delegate.name().toString());
        obj.addProperty("meta", item.getItemMeta());

        if (item.hasTrait(ResourceTrait.LongAmount)) {
            obj.addProperty("amountLong", item.asLong().getAmountLong());
        } else if (item.hasTrait(ResourceTrait.IntAmount)) {
            obj.addProperty("amountInt", item.asInt().getAmountInt());
        }

        if (item.getTag() != null) {
            obj.add("tag", NBTPersist.toJsonObjectExact(item.getTag()));
        }

        if (item.getCapTag() != null) {
            obj.add("cap", NBTPersist.toJsonObjectExact(item.getCapTag()));
        }

        return obj;
    }
}
