package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record StorageEntry(IStackKey key, long amount, boolean isFilter) {}
