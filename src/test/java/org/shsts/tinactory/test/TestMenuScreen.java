package org.shsts.tinactory.test;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.ContainerMenuScreen;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class TestMenuScreen<T extends BlockEntity> extends ContainerMenuScreen<ContainerMenu<T>> {
    public TestMenuScreen(ContainerMenu<T> menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
