package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.registrate.common.BlockEntitySet;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ComponentSet(
        RegistryEntry<Item> motor,
        RegistryEntry<Item> pump,
        RegistryEntry<Item> piston,
        RegistryEntry<Item> conveyor,
        RegistryEntry<Item> robotArm,
        RegistryEntry<Item> sensor,
        RegistryEntry<Item> fieldGenerator,
        RegistryEntry<CableBlock> cable,
        BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>> machineHull
) {
}
