package org.shsts.tinactory.datagen;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.shsts.tinactory.datagen.content.AllData;
import org.shsts.tinactory.datagen.content.AllDataMeta;
import org.shsts.tinactory.datagen.provider.FusionConnectingModelProvider;
import org.shsts.tinycorelib.api.ITinyCoreLib;
import org.shsts.tinycorelib.datagen.api.IDataGen;
import org.shsts.tinycorelib.datagen.api.IDataHandler;
import org.shsts.tinycorelib.datagen.api.ITinyDataGen;
import org.slf4j.Logger;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod(TinactoryDatagen.ID)
public class TinactoryDatagen {
    public static final String ID = "tinactory_datagen";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ITinyCoreLib CORE;
    public static ITinyDataGen DATA_CORE;
    public static IDataGen DATA_GEN;
    public static IDataHandler<FusionConnectingModelProvider> FUSION_MODELS;

    public TinactoryDatagen(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onConstruct);
        modEventBus.addListener(this::init);
        modEventBus.addListener(this::onGatherData);
    }

    private void onConstruct(FMLConstructModEvent event) {
        CORE = ITinyCoreLib.get();
        AllDataMeta.init();
    }

    private void init(FMLCommonSetupEvent event) {
        LOGGER.info("hello TinactoryDatagen!");
    }

    private void onGatherData(GatherDataEvent event) {
        DATA_CORE = ITinyDataGen.get();
        DATA_GEN = DATA_CORE.dataGen(REGISTRATE, true);
        FUSION_MODELS = DATA_GEN.createHandler(FusionConnectingModelProvider::new);
        FUSION_MODELS.addCallback(provider -> provider.addModel(modLoc("block/cube_ctm"),
            mcLoc("block/cube_all"), modLoc("block/void"), modLoc("block/void")));
        fusionModel("multiblock/coil/cupronickel", "casings/coils/machine_coil_cupronickel",
            "casings/coils/machine_coil_cupronickel_bloom");
        fusionModel("multiblock/coil/kanthal", "casings/coils/machine_coil_kanthal",
            "casings/coils/machine_coil_kanthal_bloom");
        fusionModel("multiblock/coil/nichrome", "casings/coils/machine_coil_nichrome",
            "casings/coils/machine_coil_nichrome_bloom");
        fusionModel("multiblock/coil/tungsten", "casings/coils/machine_coil_rtm_alloy", null);
        fusionModel("multiblock/coil/naquadah", "casings/coils/machine_coil_naquadah",
            "casings/coils/machine_coil_naquadah_bloom");
        fusionModel("multiblock/misc/fusion_casing", "casings/fusion/machine_casing_fusion",
            "casings/fusion/machine_casing_fusion_bloom");
        fusionModel("multiblock/misc/fusion_glass", "casings/transparent/fusion_glass", null,
            mcLoc("cutout"));
        fusionModel("multiblock/misc/hardened_glass", "casings/transparent/tempered_glass", null,
            mcLoc("translucent"));
        fusionModel("multiblock/misc/clear_glass", "multiblock/glass/quartz_glass_a", null,
            mcLoc("cutout"));

        AllData.init();
        DATA_GEN.onGatherData(event);
    }

    private void fusionModel(String id, String texture, String emissive) {
        fusionModel(id, texture, emissive, null);
    }

    private void fusionModel(String id, String texture, String emissive, ResourceLocation renderType) {
        FUSION_MODELS.addCallback(provider -> provider.addModel(modLoc("block/" + id),
            modLoc("block/cube_ctm"), gregtech(texture),
            emissive == null ? modLoc("block/void") : gregtech(emissive), renderType));
    }
}
