package org.shsts.tinactory.test.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.shsts.tinactory.test.StructureHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureUtils.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MixinStructureUtils {
    @Inject(method = "getStructureTemplate", at = @At("HEAD"), cancellable = true)
    private static void injectGetStructureTemplate(String name, ServerLevel world,
        CallbackInfoReturnable<StructureTemplate> ci) {
        ci.setReturnValue(StructureHelper.EMPTY);
    }
}
