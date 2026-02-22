package org.shsts.tinactory.core.autocraft.exec;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IJobEvents;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SequentialCraftExecutor implements ICraftExecutor {
    private final IInventoryView inventory;
    private final IMachineAllocator machineAllocator;
    private final IJobEvents jobEvents;
    private CraftPlan plan = new CraftPlan(java.util.List.of());
    private int nextStep = 0;
    private ExecutionState state = ExecutionState.IDLE;
    @Nullable
    private ExecutionError error;

    public SequentialCraftExecutor(IInventoryView inventory, IMachineAllocator machineAllocator, IJobEvents jobEvents) {
        this.inventory = inventory;
        this.machineAllocator = machineAllocator;
        this.jobEvents = jobEvents;
    }

    @Override
    public void start(CraftPlan plan) {
        this.plan = plan;
        nextStep = 0;
        error = null;
        state = plan.steps().isEmpty() ? ExecutionState.COMPLETED : ExecutionState.RUNNING;
    }

    @Override
    public void tick() {
        if (state != ExecutionState.RUNNING) {
            return;
        }
        if (nextStep >= plan.steps().size()) {
            state = ExecutionState.COMPLETED;
            return;
        }
        var step = plan.steps().get(nextStep);
        if (!machineAllocator.canRun(step.pattern().machineRequirement())) {
            error = new ExecutionError(
                ExecutionError.Code.MACHINE_UNAVAILABLE,
                step.stepId(),
                "Machine requirement is unavailable"
            );
            jobEvents.onStepBlocked(step, error.message());
            state = ExecutionState.BLOCKED;
            return;
        }
        for (var input : step.pattern().inputs()) {
            var required = input.amount() * step.runs();
            if (inventory.amountOf(input.key()) < required) {
                error = new ExecutionError(
                    ExecutionError.Code.INPUT_MISSING,
                    step.stepId(),
                    "Input resources are unavailable"
                );
                jobEvents.onStepBlocked(step, error.message());
                state = ExecutionState.BLOCKED;
                return;
            }
        }
        jobEvents.onStepStarted(step);
        for (var input : step.pattern().inputs()) {
            inventory.consume(input.key(), input.amount() * step.runs());
        }
        for (var output : step.pattern().outputs()) {
            inventory.produce(output.key(), output.amount() * step.runs());
        }
        jobEvents.onStepCompleted(step);
        nextStep++;
        if (nextStep >= plan.steps().size()) {
            state = ExecutionState.COMPLETED;
        }
    }

    @Override
    public void cancel() {
        if (state == ExecutionState.RUNNING || state == ExecutionState.BLOCKED) {
            error = new ExecutionError(ExecutionError.Code.CANCELLED, nextStepId(), "Execution cancelled");
            state = ExecutionState.CANCELLED;
        }
    }

    @Override
    public ExecutionState state() {
        return state;
    }

    @Override
    public @Nullable ExecutionError error() {
        return error;
    }

    private String nextStepId() {
        if (nextStep >= plan.steps().size()) {
            return "complete";
        }
        return plan.steps().get(nextStep).stepId();
    }
}
