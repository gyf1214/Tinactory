package org.shsts.tinactory.core.recipe;

import com.google.gson.JsonElement;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TypedSerializer<T> implements ICombinedSerializer<T> {
    private final Map<String, ICombinedSerializer<? extends T>> typeMap = new HashMap<>();
    private final Map<Class<? extends T>, ICombinedSerializer<? extends T>> classMap = new HashMap<>();

    public TypedSerializer() {
        registerSerializers();
    }

    protected abstract void registerSerializers();

    protected <T1 extends T> void registerSerializer(Class<T1> clazz, ICombinedSerializer<T1> serializer) {
        typeMap.put(serializer.getTypeName(), serializer);
        classMap.put(clazz, serializer);
    }

    @SuppressWarnings("unchecked")
    protected static <T1, T2> void toNetwork(ICombinedSerializer<T1> serializer, T2 sth, FriendlyByteBuf buf) {
        buf.writeUtf(serializer.getTypeName());
        serializer.toNetwork((T1) sth, buf);
    }

    @Override
    public void toNetwork(T sth, FriendlyByteBuf buf) {
        toNetwork(classMap.get(sth.getClass()), sth, buf);
    }

    @Override
    public T fromNetwork(FriendlyByteBuf buf) {
        var type = buf.readUtf();
        return typeMap.get(type).fromNetwork(buf);
    }

    @SuppressWarnings("unchecked")
    protected static <T1, T2> JsonElement toJson(ICombinedSerializer<T1> serializer, T2 sth) {
        var jo = serializer.toJson((T1) sth).getAsJsonObject();
        jo.addProperty("type", serializer.getTypeName());
        return jo;
    }

    @Override
    public JsonElement toJson(T sth) {
        return toJson(classMap.get(sth.getClass()), sth);
    }

    @Override
    public T fromJson(JsonElement je) {
        var jo = je.getAsJsonObject();
        var type = GsonHelper.getAsString(jo, "type");
        return typeMap.get(type).fromJson(jo);
    }
}
