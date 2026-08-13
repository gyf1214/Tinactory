package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record WorkbenchTransferResult(Code code, List<Integer> missingIndexes) {
    public enum Code {
        SUCCESS, MISSING_INPUT, INVENTORY_FULL
    }

    public WorkbenchTransferResult {
        missingIndexes = List.copyOf(missingIndexes);
    }

    public static WorkbenchTransferResult success() {
        return new WorkbenchTransferResult(Code.SUCCESS, List.of());
    }

    public static WorkbenchTransferResult missingInput(List<Integer> missingIndexes) {
        return new WorkbenchTransferResult(Code.MISSING_INPUT, missingIndexes);
    }

    public static WorkbenchTransferResult inventoryFull() {
        return new WorkbenchTransferResult(Code.INVENTORY_FULL, List.of());
    }
}
