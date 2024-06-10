package org.shsts.tinactory.datagen;

import net.minecraftforge.eventbus.api.IEventBus;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.datagen.content.Technologies;
import org.shsts.tinactory.datagen.content.Veins;
import org.shsts.tinactory.registrate.Registrate;

public final class DataGen {
    public static final Registrate REGISTRATE = new Registrate(Tinactory.ID);

    public static void init(IEventBus modEventBus) {
        Technologies.init();
        Veins.init();

        REGISTRATE.register(modEventBus);
    }
}
