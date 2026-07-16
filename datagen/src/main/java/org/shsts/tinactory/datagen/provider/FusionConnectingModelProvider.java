package org.shsts.tinactory.datagen.provider;

import com.supermartijn642.fusion.api.model.DefaultModelTypes;
import com.supermartijn642.fusion.api.model.ModelInstance;
import com.supermartijn642.fusion.api.model.types.connecting.ConnectingModelData;
import com.supermartijn642.fusion.api.provider.FusionModelProvider;
import com.supermartijn642.fusion.api.texture.types.connecting.predicates.DefaultConnectionPredicates;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.shsts.tinycorelib.datagen.api.IDataGen;
import org.shsts.tinycorelib.datagen.api.IDataHandler;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class FusionConnectingModelProvider extends FusionModelProvider {
    private final String modId;
    private final IDataHandler<FusionConnectingModelProvider> handler;

    public FusionConnectingModelProvider(IDataGen dataGen,
        IDataHandler<FusionConnectingModelProvider> handler,
        GatherDataEvent event) {
        super(dataGen.modid(), event.getGenerator().getPackOutput(), event.getExistingFileHelper());
        this.modId = dataGen.modid();
        this.handler = handler;
    }

    public void addConnecting(String id, ConnectingModelData.Builder<?, ConnectingModelData> model) {
        var loc = ResourceLocation.fromNamespaceAndPath(modId, id);
        addModel(loc, ModelInstance.of(DefaultModelTypes.CONNECTING, model
            .defaultConnections(DefaultConnectionPredicates.isSameBlock())
            .build()));
    }

    @Override
    protected void generate() {
        handler.register(this);
    }
}
