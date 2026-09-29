package org.shsts.tinactory.compat.ftbquests;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.TinactoryKeys;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TechTaskRegistration {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID, "technology");

    @Nullable
    private static TaskType type;

    private TechTaskRegistration() {}

    public static void register() {
        type = TaskTypes.register(ID, TechTask::new, () -> Icon.getIcon("minecraft:item/knowledge_book"));
    }

    public static TaskType type() {
        return Objects.requireNonNull(type);
    }
}
