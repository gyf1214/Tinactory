package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class UnsupportedTypeException extends RuntimeException {
    public UnsupportedTypeException(String field, String value) {
        super("Unsupported " + field + ": " + value);
    }
}
