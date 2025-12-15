package org.shsts.tinactory.datagen.content.material

import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import net.minecraftforge.common.Tags
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.RegistryHelper.getItem
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.autofarm
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.cutter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.extractor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.macerator
import org.shsts.tinactory.datagen.content.component.Components.COMPONENT_TICKS

object Crops {
    fun init() {
        // auto farms
        farm(Items.WHEAT, Items.WHEAT_SEEDS, true)
        farm(Items.BEETROOT, Items.BEETROOT_SEEDS, true)
        farm(Items.PUMPKIN, Items.PUMPKIN_SEEDS, false)
        farm(Items.MELON, Items.MELON_SEEDS, false)
        farm(Items.CARROT)
        farm(Items.COCOA_BEANS)
        farm(Items.CACTUS)
        farm(Items.POTATO)
        farm(Items.KELP)
        farm(Items.SEA_PICKLE)
        farm(Items.SUGAR_CANE)
        farm(Items.SWEET_BERRIES)
        farm(Items.GLOW_BERRIES)
        farm(Items.BROWN_MUSHROOM)
        farm(Items.RED_MUSHROOM)
        farm(Items.NETHER_WART)
        farm(Items.CRIMSON_FUNGUS)
        farm(Items.WARPED_FUNGUS)

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
        toSeed(Items.PUMPKIN, Items.PUMPKIN_SEEDS, 4)
        toSeed(Items.MELON_SLICE, Items.MELON_SEEDS)
        toSeed(Items.MELON, Items.MELON_SEEDS, 9)

        // crop to biomass
        biomass(Items.WHEAT, 1, 1.0, 48)
        biomass(Items.BEETROOT, 1, 1.5, 48)
        biomass(Items.PUMPKIN, 1, 1.0, 240)
        biomass(Items.MELON, 1, 2.0, 240)
        biomass(Items.CARROT, 1, 0.5, 64)
        biomass(Items.COCOA_BEANS, 1, 0.5, 64)
        biomass(Items.CACTUS, 1, 0.5, 64)
        biomass(Items.POTATO, 1, 0.75, 64)
        biomass(Items.KELP, 1, 0.75, 64)
        biomass(Items.SEA_PICKLE, 1, 0.75, 64)
        biomass(Items.SUGAR_CANE, 1, 1.0, 96)
        biomass(Items.SWEET_BERRIES, 2, 1.5, 64)
        biomass(Items.GLOW_BERRIES, 2, 1.5, 64)
        biomass(Items.NETHER_WART, 2, 0.75, 96)
        biomass(Items.CRIMSON_FUNGUS, 2, 0.75, 96)
        biomass(Items.WARPED_FUNGUS, 2, 0.75, 96)
        extractor {
            input(Tags.Items.MUSHROOMS, 2) {
                output("biomass", amount = 0.75)
                voltage(Voltage.LV)
                workTicks(96)
            }
        }
        // seeds to biomass
        biomass(Items.WHEAT_SEEDS, 16, 1.6, 64)
        biomass(Items.BEETROOT_SEEDS, 16, 1.6, 64)
        biomass(Items.MELON_SEEDS, 16, 1.6, 64)
        biomass(Items.PUMPKIN_SEEDS, 16, 1.6, 64)

        // misc
        assembler {
            defaults {
                voltage(Voltage.ULV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.SOLDERING)
            }
            output(Items.HAY_BLOCK) {
                input(Items.WHEAT, 9)
            }
            output(Items.WHEAT, 9) {
                input(Items.HAY_BLOCK)
            }
        }
    }

    private fun farm(crop: ItemLike, seed: ItemLike = crop, outputSeed: Boolean = false) {
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
                workTicks(320)
            }
        }
        autofarm {
            input(seed, suffix = "_with_fertilizer") {
                input("water", amount = 0.5)
                input(getItem("misc/fertilizer"), port = 2)
                output(crop, if (crop == seed) 6 else 2)
                if (outputSeed) {
                    output(seed, 4)
                }
                voltage(Voltage.LV)
                workTicks(320)
            }
        }
    }

    private fun toSeed(crop: Item, seed: Item, amount: Int = 1, workTicks: Long = 64L * amount) {
        macerator {
            input(crop) {
                output(seed, amount)
                voltage(Voltage.LV)
                workTicks(workTicks)
            }
        }
    }

    private fun biomass(crop: Item, amount: Int, outAmount: Double, workTicks: Long) {
        extractor {
            input(crop, amount) {
                output("biomass", amount = outAmount)
                voltage(Voltage.LV)
                workTicks(workTicks)
            }
        }
    }
}
