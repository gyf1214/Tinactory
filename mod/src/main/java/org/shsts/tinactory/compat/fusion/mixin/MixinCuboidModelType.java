package org.shsts.tinactory.compat.fusion.mixin;

import com.supermartijn642.fusion.api.model.custom.DefaultModelProperties;
import com.supermartijn642.fusion.api.util.Property;
import com.supermartijn642.fusion.model.types.cuboid.CuboidModelType;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.neoforged.neoforge.client.NamedRenderTypeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(CuboidModelType.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MixinCuboidModelType {
    @Inject(method = "getProperty", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectGetProperty(Property<?, ?> property, Object context,
        BlockModel model, CallbackInfoReturnable<Optional<?>> ci) {
        if (property != DefaultModelProperties.NEO_MODEL_RENDER_TYPE) {
            return;
        }
        var hint = model.customData.getRenderTypeHint();
        ci.setReturnValue(hint == null ? Optional.empty() : Optional.of(NamedRenderTypeManager.get(hint)));
    }
}
