package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.LayoutSetBuilder;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinycorelib.api.core.Transformer;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.api.logistics.SlotType.FLUID_INPUT;
import static org.shsts.tinactory.api.logistics.SlotType.FLUID_OUTPUT;
import static org.shsts.tinactory.api.logistics.SlotType.ITEM_INPUT;
import static org.shsts.tinactory.api.logistics.SlotType.ITEM_OUTPUT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllLayouts {
    public static final Layout WORKBENCH;
    public static final Layout BOILER;
    public static final Layout BLAST_FURNACE;
    public static final Layout SIFTER;
    public static final Layout VACUUM_FREEZER;
    public static final List<Layout> DISTILLATION_TOWER;

    static {
        WORKBENCH = Layout.builder()
            .dummySlot(9 + 6 * SLOT_SIZE, SLOT_SIZE)
            .image(16 + 4 * SLOT_SIZE, 20, Texture.CRAFTING_ARROW)
            .port(ITEM_INPUT)
            .slots(0, 3 * SLOT_SIZE + SPACING, 1, 9)
            .slots(9 + SLOT_SIZE, 0, 3, 3)
            .buildLayout();

        BOILER = Layout.builder()
            .port(ITEM_INPUT)
            .slot(0, 1 + SLOT_SIZE * 2)
            .port(FLUID_INPUT)
            .slot(0, 1)
            .port(FLUID_OUTPUT)
            .slot(3 * SLOT_SIZE, 1 + SLOT_SIZE)
            .buildLayout();

        BLAST_FURNACE = Layout.builder()
            .port(ITEM_INPUT)
            .slots(0, 1, 1, 3)
            .port(FLUID_INPUT)
            .slot(2 * SLOT_SIZE, 1 + SLOT_SIZE)
            .port(ITEM_OUTPUT)
            .slots(5 * SLOT_SIZE, 1, 1, 3)
            .port(FLUID_OUTPUT)
            .slot(5 * SLOT_SIZE, 1 + SLOT_SIZE)
            .progressBar(Texture.PROGRESS_ARROW, 8 + 3 * SLOT_SIZE, SLOT_SIZE / 2)
            .buildLayout();

        SIFTER = Layout.builder()
            .port(ITEM_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slots(3 * SLOT_SIZE, 1, 2, 3)
            .progressBar(Texture.PROGRESS_SIFT, 8 + SLOT_SIZE, SLOT_SIZE / 2)
            .buildLayout();

        VACUUM_FREEZER = Layout.builder()
            .port(ITEM_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(FLUID_INPUT)
            .slot(SLOT_SIZE, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slot(4 * SLOT_SIZE, 1 + SLOT_SIZE / 2)
            .port(FLUID_OUTPUT)
            .slot(5 * SLOT_SIZE, 1 + SLOT_SIZE / 2)
            .progressBar(Texture.PROGRESS_ARROW, 8 + 2 * SLOT_SIZE, SLOT_SIZE / 2)
            .buildLayout();

        DISTILLATION_TOWER = new ArrayList<>();
        for (var k = 0; k < 6; k++) {
            DISTILLATION_TOWER.add(distillationLayout(k + 1));
        }
    }

    private static Layout distillationLayout(int slots) {
        var yOffset = slots > 3 ? SLOT_SIZE : 0;
        return Layout.builder()
            .port(FLUID_INPUT)
            .slot(0, 1 + yOffset + SLOT_SIZE / 2 + 4)
            .port(FLUID_OUTPUT)
            .transform(distillationSlots(slots, 0))
            .port(ITEM_OUTPUT)
            .transform(distillationSlots(slots, SLOT_SIZE + yOffset + 8))
            .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, yOffset + SLOT_SIZE / 2 + 4)
            .buildLayout();
    }

    private static <P> Transformer<LayoutSetBuilder<P>> distillationSlots(int slots, int y1) {
        return $ -> {
            for (var i = 0; i < slots; i++) {
                var x = i % 3;
                var y = i / 3;
                $.slot((3 + x) * SLOT_SIZE, 1 + y1 + y * SLOT_SIZE);
            }
            return $;
        };
    }
}
