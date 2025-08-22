package org.shsts.tinactory.datagen.content.material

import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import net.minecraftforge.common.Tags
import org.shsts.tinactory.content.AllItems.FERTILIZER
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.autofarm
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.cutter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.extractor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.macerator

object Crops {
    fun init() {
        // auto farms
        farm(Items.WHEAT, Items.WHEAT_SEEDS, true)
        farm(Items.BEETROOT, Items.BEETROOT_SEEDS, true)
        farm(Items.PUMPKIN, Items.PUMPKIN_SEEDS, false)
        farm(Items.MELON, Items.MELON_SEEDS, false)
        farm(Items.CARROT)
        farm(Items.POTATO)
        farm(Items.COCOA_BEANS)
        farm(Items.SUGAR_CANE)
        farm(Items.SWEET_BERRIES)
        farm(Items.CACTUS)
        farm(Items.KELP)
        farm(Items.SEA_PICKLE)
        farm(Items.NETHER_WART)
        farm(Items.CRIMSON_FUNGUS)
        farm(Items.WARPED_FUNGUS)
        farm(Items.GLOW_BERRIES)
        farm(Items.BROWN_MUSHROOM)
        farm(Items.RED_MUSHROOM)

        // cut melon
        cutter {
            output(Items.MELON_SLICE, 9) {
                input(Items.MELON)
                voltage(Voltage.LV)
                workTicks(128)
            }
        }

        // crop to seed
        toSeed(Items.WHEAT, Items.WHEAT_SEEDS)
        toSeed(Items.BEETROOT, Items.BEETROOT_SEEDS)
        toSeed(Items.PUMPKIN, Items.PUMPKIN_SEEDS, 4, 256)
        toSeed(Items.MELON_SLICE, Items.MELON_SEEDS)

        // crop to biomass
        toBiomass(Items.WHEAT, 1, 0.1, 48)
        toBiomass(Items.BEETROOT, 1, 0.5, 48)
        toBiomass(Items.CARROT, 2, 0.1, 96)
        toBiomass(Items.POTATO, 2, 0.15, 64)
        toBiomass(Items.MELON, 1, 0.6, 240)
        toBiomass(Items.PUMPKIN, 1, 0.3, 256)
        toBiomass(Items.COCOA_BEANS, 2, 0.1, 128)
        toBiomass(Items.SUGAR_CANE, 1, 0.4, 48)
        toBiomass(Items.SWEET_BERRIES, 1, 0.1, 32)
        toBiomass(Items.CACTUS, 1, 0.1, 128)
        toBiomass(Items.KELP, 2, 0.15, 64)
        toBiomass(Items.SEA_PICKLE, 2, 0.1, 96)
        toBiomass(Items.NETHER_WART, 4, 0.1, 96)
        toBiomass(Items.CRIMSON_FUNGUS, 4, 0.1, 128)
        toBiomass(Items.WARPED_FUNGUS, 4, 0.1, 128)
        toBiomass(Items.GLOW_BERRIES, 2, 0.15, 64)
        // seeds to biomass
        toBiomass(Items.WHEAT_SEEDS, 16, 0.1, 64)
        toBiomass(Items.BEETROOT_SEEDS, 16, 0.1, 64)
        toBiomass(Items.MELON_SEEDS, 16, 0.1, 64)
        toBiomass(Items.PUMPKIN_SEEDS, 16, 0.1, 64)

        extractor {
            input(Tags.Items.MUSHROOMS, 6) {
                output("biomass", amount = 0.1)
                voltage(Voltage.MV)
                workTicks(96)
            }
        }
    }

    private fun farm(crop: ItemLike, seed: ItemLike = crop, outputSeed: Boolean = true) {
        autofarm {
            defaults {
                output(crop, if (crop == seed) 3 else 1)
                if (outputSeed) {
                    output(seed, 2)
                }
                voltage(Voltage.LV)
            }
            input(seed) {
                input("biomass", amount = 0.5)
                workTicks(800)
            }
            input(seed, suffix = "_with_bone_meal") {
                input("water", amount = 0.5)
                input(Items.BONE_MEAL, 1, port = 2)
                workTicks(300)
            }
        }
        autofarm {
            input(seed, suffix = "_with_fertilizer") {
                input("water", amount = 0.5)
                input(FERTILIZER.get(), port = 2)
                output(crop, if (crop == seed) 6 else 2)
                if (outputSeed) {
                    output(seed, 4)
                }
                voltage(Voltage.MV)
                workTicks(300)
            }
        }
    }

    private fun toSeed(crop: Item, seed: Item, amount: Int = 1, workTicks: Long = 64L * amount) {
        macerator {
            output(seed, amount) {
                input(crop)
                voltage(Voltage.LV)
                workTicks(workTicks)
            }
        }
    }

    private fun toBiomass(crop: Item, amount: Int, outAmount: Double, workTicks: Long) {
        extractor {
            input(crop, amount) {
                output("biomass", amount = outAmount)
                voltage(Voltage.MV)
                workTicks(workTicks)
            }
        }
    }
}
