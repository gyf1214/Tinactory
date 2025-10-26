package org.shsts.tinactory.content.sound;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.core.common.MetaConsumer;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllRegistries.SOUND_EVENTS;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SoundMeta extends MetaConsumer {
    public SoundMeta() {
        super("Sound");
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        for (var je : GsonHelper.getAsJsonArray(jo, "sounds")) {
            var key = GsonHelper.convertToString(je, "sounds");
            REGISTRATE.registryEntry(SOUND_EVENTS, key, () -> new SoundEvent(modLoc(key)));
        }
    }
}
