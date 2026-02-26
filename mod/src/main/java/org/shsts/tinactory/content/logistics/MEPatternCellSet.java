package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record MEPatternCellSet(IEntry<Item> component, IEntry<MEPatternCell> pattern) {}
