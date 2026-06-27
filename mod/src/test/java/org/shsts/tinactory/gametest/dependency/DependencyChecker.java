package org.shsts.tinactory.gametest.dependency;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.AllItems;
import org.shsts.tinactory.AllMaterials;
import org.shsts.tinactory.AllMultiblocks;
import org.shsts.tinactory.AllRecipes;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.multiblock.Cleanroom;
import org.shsts.tinactory.content.multiblock.LensBlock;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe;
import org.shsts.tinactory.content.recipe.CleanRecipe;
import org.shsts.tinactory.content.recipe.EngravingRecipe;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.shsts.tinactory.content.tool.NuclearRod;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.multiblock.BlockIngredient;
import org.shsts.tinactory.integration.recipe.ItemsIngredient;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;
import org.shsts.tinactory.integration.recipe.TagIngredient;
import org.shsts.tinactory.integration.tech.TechManagers;
import org.shsts.tinycorelib.api.recipe.IRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeManager;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

public final class DependencyChecker {
    private static final Logger LOGGER = LogUtils.getLogger();

    private record LithographyLensBlock(StackNode node, Collection<Item> lens) {}

    private static final ResourceLocation BOILER = AllRecipes.BOILER.loc();
    private static final ResourceLocation LARGE_BOILER = modLoc("large_boiler");
    private static final ResourceLocation LARGE_CHEMICAL_REACTOR = modLoc("large_chemical_reactor");
    private static final ResourceLocation MINECRAFT_SMELTING = mcLoc("smelting");
    private static final ResourceLocation MULTI_SMELTER = modLoc("multi_smelter");
    private static final ResourceLocation NUCLEAR_REACTOR = modLoc("nuclear_reactor");
    private static final ResourceLocation STICKY_RESIN = modLoc("rubber_tree/sticky_resin");
    private static final ResourceLocation TOOL_CRAFTING = modLoc("tool_crafting");
    private static final List<ResourceLocation> BOILER_BLOCKS = List.of(
        modLoc("machine/boiler/low"),
        modLoc("machine/boiler/high"));
    private static final List<ResourceLocation> BOILER_MULTIBLOCKS = List.of(
        LARGE_BOILER,
        NUCLEAR_REACTOR);
    private static final List<ResourceLocation> GENERATOR_RECIPE_TYPES = List.of(
        modLoc("combustion_generator"),
        modLoc("gas_turbine"),
        modLoc("steam_turbine"));
    private static final String COIL_TEMPERATURE = "coil_temperature";
    private static final String CLEANROOM_CLEANNESS = "cleanroom_cleanness";
    private static final String TEST_MATERIAL = "test";
    private static final Voltage MAX_PROGRESS_VOLTAGE = Voltage.LUV;
    // Current LuV-progress baseline is calibrated from the runtime dependency-checker report. The remaining list
    // includes later wetware-board, advanced-SMD, Neutronium, and ZPM targets.
    private static final int ACCEPTED_UNREACHABLE_NODES = 8;

    private final List<DependencyMethod> methods = new ArrayList<>();
    private final Map<IDependencyNode, Set<DependencyMethod>> methodsByOutput = new HashMap<>();
    private List<LithographyLensBlock> lithographyLensBlocks = null;

    private void addMethod(DependencyMethod method) {
        methods.add(method);
        for (var output : method.outputs()) {
            methodsByOutput.computeIfAbsent(output, $ -> new TreeSet<>()).add(method);
        }
    }

    public static boolean runRuntimeCheck(ServerLevel world) {
        var checker = new DependencyChecker();
        checker.extractRuntimeMethods(world);
        var solver = new DependencySolver(checker.methods);
        var startNodes = checker.startNodes();
        solver.solve(startNodes);
        return checker.writeReport(solver, startNodes, checker.intendedTargets(), checker.exemptTargets());
    }

    private void extractRuntimeMethods(ServerLevel world) {
        extractProcessingRecipes(world);
        extractVanillaRecipes(world);
        addMachineBridgeMethods();
        addWorkbenchBridgeMethod();
        addMultiblockBridgeMethods();
        addBoilerBridgeMethods();
        addVoltageBridgeMethods();
        addCoilBridgeMethods();
        addCleanroomBridgeMethods();
        addNuclearReactorBridgeMethods();
        addTagBridgeMethods();
    }

    private void extractProcessingRecipes(ServerLevel world) {
        var recipeManager = CORE.recipeManager(world);
        for (var typeInfo : AllRecipes.PROCESSING_TYPES.values()) {
            for (var entry : allTinyRecipeEntries(recipeManager, typeInfo.recipeType())) {
                var recipe = entry.get();
                if (recipe instanceof ProcessingRecipe processingRecipe) {
                    addProcessingRecipeMethod(typeInfo.recipeType().loc(), entry.loc(), processingRecipe);
                }
            }
        }
        for (var entry : recipeManager.getAllRecipesFor(AllRecipes.BOILER)) {
            var recipe = entry.get();
            var requirements = new ArrayList<IDependencyNode>();
            requirements.add(new MachineNode(BOILER, Voltage.PRIMITIVE));
            stackNode(recipe.input).ifPresent(requirements::add);
            var outputs = new ArrayList<IDependencyNode>();
            stackNode(recipe.output).ifPresent(outputs::add);
            addMethodIfUseful(entry.loc() + "#boiler", requirements, outputs, "boiler recipe " + entry.loc());
        }
    }

    private void addProcessingRecipeMethod(ResourceLocation recipeTypeId, ResourceLocation recipeId,
        ProcessingRecipe recipe) {
        var requirements = new ArrayList<IDependencyNode>();
        requirements.add(new MachineNode(recipeTypeId, Voltage.fromValue(recipe.voltage)));
        addProcessingIngredients(recipeId, recipe, requirements, false);
        addProcessingSubtypeRequirements(recipe, requirements, true);
        var outputs = new ArrayList<IDependencyNode>();
        if (recipe instanceof ResearchRecipe researchRecipe) {
            outputs.add(new TechnologyNode(researchRecipe.target));
        } else {
            recipe.outputs.stream()
                .map(ProcessingRecipe.Output::result)
                .map(this::resultNode)
                .flatMap(Optional::stream)
                .forEach(outputs::add);
        }
        addMethodIfUseful(recipeId + "#processing", requirements, outputs, "processing recipe " + recipeId);
        if (recipe instanceof EngravingRecipe engravingRecipe) {
            addLithographyRecipeMethods(recipeTypeId, recipeId, engravingRecipe, outputs);
        }
        if (recipe instanceof GeneratorRecipe generatorRecipe) {
            addGeneratorRecipeMethods(recipeTypeId, recipeId, generatorRecipe);
        }
    }

    private void addLithographyRecipeMethods(ResourceLocation recipeTypeId, ResourceLocation recipeId,
        EngravingRecipe recipe, Collection<IDependencyNode> outputs) {
        var lensInput = recipe.inputs.stream()
            .filter(input -> input.port() == 1)
            .findFirst();
        if (lensInput.isEmpty()) {
            return;
        }
        var voltage = Voltage.fromValue(recipe.voltage);
        for (var entry : AllMultiblocks.LITHOGRAPHY_CLEANNESS_FACTORS.entrySet()) {
            var multiblockSet = AllMultiblocks.MULTIBLOCK_SETS.get(entry.getKey());
            if (multiblockSet == null || entry.getValue() <= 0d ||
                multiblockSet.types().stream().noneMatch(type -> type.loc().equals(recipeTypeId))) {
                continue;
            }
            var multiblock = new MultiblockNode(modLoc(entry.getKey()));
            for (var lensBlock : lithographyLensBlocks()) {
                if (!matchLens(lensBlock.lens(), lensInput.get().ingredient())) {
                    continue;
                }
                var requirements = new ArrayList<IDependencyNode>();
                requirements.add(multiblock);
                requirements.add(new VoltageNode(voltage));
                requirements.add(lensBlock.node());
                if (recipe.minCleanness > 0d) {
                    requirements.add(new NumericNode(CLEANROOM_CLEANNESS, recipe.minCleanness / entry.getValue()));
                }
                addProcessingIngredients(recipeId, recipe, requirements, true);
                addProcessingSubtypeRequirements(recipe, requirements, false);
                for (var machineInterface : multiblockInterfaceNodes(voltage)) {
                    addMethodIfUseful(
                        recipeId + "#lithography/" + entry.getKey() + "/" + lensBlock.node().id() +
                            "/" + machineInterface.id(),
                        with(requirements, machineInterface), outputs, "lithography recipe " + recipeId);
                }
            }
        }
    }

    private void addGeneratorRecipeMethods(ResourceLocation recipeTypeId, ResourceLocation recipeId,
        GeneratorRecipe recipe) {
        var recipeVoltage = Voltage.fromValue(recipe.voltage);
        for (var voltage : Voltage.values()) {
            if (!canRunGeneratorRecipe(recipe, recipeVoltage, voltage)) {
                continue;
            }
            var requirements = new ArrayList<IDependencyNode>();
            requirements.add(new MachineNode(recipeTypeId, voltage));
            for (var i = 0; i < recipe.inputs.size(); i++) {
                ingredientNode(recipe.inputs.get(i).ingredient(), recipeId, i).ifPresent(requirements::add);
            }
            var output = new GeneratorNode(voltage);
            addMethodIfUseful(recipeId + "#generator/" + voltage.id, requirements, List.of(output),
                "generator recipe " + recipeId);
        }
    }

    private static boolean canRunGeneratorRecipe(GeneratorRecipe recipe, Voltage recipeVoltage,
        Voltage machineVoltage) {
        if (machineVoltage == Voltage.PRIMITIVE || machineVoltage == Voltage.MAX) {
            return false;
        }
        return recipe.exactVoltage() ?
            machineVoltage == recipeVoltage :
            machineVoltage.rank >= recipeVoltage.rank;
    }

    private void addProcessingIngredients(ResourceLocation recipeId, ProcessingRecipe recipe,
        Collection<IDependencyNode> requirements, boolean skipLens) {
        for (var i = 0; i < recipe.inputs.size(); i++) {
            var input = recipe.inputs.get(i);
            if (skipLens && input.port() == 1) {
                continue;
            }
            ingredientNode(input.ingredient(), recipeId, i).ifPresent(requirements::add);
        }
    }

    private void addProcessingSubtypeRequirements(ProcessingRecipe recipe, Collection<IDependencyNode> requirements,
        boolean includeClean) {
        if (recipe instanceof AssemblyRecipe assemblyRecipe) {
            assemblyRecipe.requiredTech.stream()
                .map(TechnologyNode::new)
                .forEach(requirements::add);
        }
        if (recipe instanceof ResearchRecipe researchRecipe) {
            TechManagers.server().techByKey(researchRecipe.target)
                .stream()
                .flatMap(technology -> technology.getDepends().stream())
                .map(technology -> new TechnologyNode(technology.loc()))
                .forEach(requirements::add);
        }
        if (recipe instanceof BlastFurnaceRecipe blastFurnaceRecipe && blastFurnaceRecipe.temperature > 0) {
            requirements.add(new NumericNode(COIL_TEMPERATURE, blastFurnaceRecipe.temperature));
        }
        if (includeClean && recipe instanceof CleanRecipe cleanRecipe && cleanRecipe.minCleanness > 0d) {
            requirements.add(new NumericNode(CLEANROOM_CLEANNESS, cleanRecipe.minCleanness));
        }
        if (recipe instanceof ChemicalReactorRecipe chemicalReactorRecipe && chemicalReactorRecipe.requireMultiblock) {
            requirements.add(new MultiblockNode(LARGE_CHEMICAL_REACTOR));
        }
    }

    private void extractVanillaRecipes(ServerLevel world) {
        var recipeManager = world.getRecipeManager();
        for (var recipe : recipeManager.getAllRecipesFor(RecipeType.SMELTING)) {
            addSmeltingRecipeMethod(recipe);
        }
        for (var recipe : recipeManager.getAllRecipesFor(RecipeType.CRAFTING)) {
            addCraftingRecipeMethod(recipe);
        }
        for (var entry : CORE.recipeManager(world).getAllRecipesFor(AllRecipes.TOOL_CRAFTING)) {
            addToolRecipeMethod(entry.loc(), entry.get());
        }
    }

    private void addSmeltingRecipeMethod(SmeltingRecipe recipe) {
        var requirements = new ArrayList<IDependencyNode>();
        ingredientNode(recipe.getIngredients().get(0), recipe.getId(), 0).ifPresent(requirements::add);
        requirements.add(new MachineNode(MINECRAFT_SMELTING, Voltage.PRIMITIVE));
        var outputs = new ArrayList<IDependencyNode>();
        stackNode(recipe.getResultItem()).ifPresent(outputs::add);
        addMethodIfUseful(recipe.getId() + "#smelting", requirements, outputs, "smelting recipe " + recipe.getId());
    }

    private void addCraftingRecipeMethod(CraftingRecipe recipe) {
        var requirements = new ArrayList<IDependencyNode>();
        var ingredients = recipe.getIngredients();
        for (var i = 0; i < ingredients.size(); i++) {
            var ingredient = ingredients.get(i);
            if (!ingredient.isEmpty()) {
                ingredientNode(ingredient, recipe.getId(), i).ifPresent(requirements::add);
            }
        }
        var outputs = new ArrayList<IDependencyNode>();
        stackNode(recipe.getResultItem()).ifPresent(outputs::add);
        addMethodIfUseful(recipe.getId() + "#crafting", requirements, outputs, "crafting recipe " + recipe.getId());
    }

    private void addToolRecipeMethod(ResourceLocation recipeId, ToolRecipe recipe) {
        var requirements = new ArrayList<IDependencyNode>();
        requirements.add(new MachineNode(TOOL_CRAFTING, Voltage.PRIMITIVE));
        var inputIndex = 0;
        for (var ingredient : recipe.shapedRecipe.getIngredients()) {
            if (!ingredient.isEmpty()) {
                ingredientNode(ingredient, recipeId, inputIndex).ifPresent(requirements::add);
            }
            inputIndex++;
        }
        for (var ingredient : recipe.toolIngredients) {
            if (!ingredient.isEmpty()) {
                ingredientNode(ingredient, recipeId, inputIndex).ifPresent(requirements::add);
            }
            inputIndex++;
        }
        var outputs = new ArrayList<IDependencyNode>();
        stackNode(recipe.shapedRecipe.getResultItem()).ifPresent(outputs::add);
        addMethodIfUseful(recipeId + "#tool_crafting", requirements, outputs,
            "tool crafting recipe " + recipeId);
    }

    private void addMachineBridgeMethods() {
        for (var machineSet : AllBlockEntities.MACHINE_SETS.values()) {
            if (machineSet instanceof ProcessingSet processingSet) {
                for (var voltage : processingSet.voltages) {
                    var requirements = new ArrayList<IDependencyNode>();
                    stackNode(new ItemStack(processingSet.block(voltage))).ifPresent(requirements::add);
                    if (!isGeneratorRecipeType(processingSet.recipeType.loc())) {
                        requirements.add(new VoltageNode(voltage));
                    }
                    var output = new MachineNode(processingSet.recipeType.loc(), voltage);
                    addMethodIfUseful("machine/" + output.id(), requirements, List.of(output), "machine bridge");
                }
            }
        }
        addVanillaFurnaceBridge();
        addElectricFurnaceBridge();
    }

    private void addWorkbenchBridgeMethod() {
        var requirements = new ArrayList<IDependencyNode>();
        stackNode(new ItemStack(AllBlockEntities.WORKBENCH.get())).ifPresent(requirements::add);
        var output = new MachineNode(TOOL_CRAFTING, Voltage.PRIMITIVE);
        addMethodIfUseful("machine/" + output.id(), requirements, List.of(output), "workbench bridge");
    }

    private void addVanillaFurnaceBridge() {
        var requirements = new ArrayList<IDependencyNode>();
        stackNode(new ItemStack(Items.FURNACE)).ifPresent(requirements::add);
        var output = new MachineNode(MINECRAFT_SMELTING, Voltage.PRIMITIVE);
        addMethodIfUseful("machine/" + output.id(), requirements, List.of(output), "vanilla furnace bridge");
    }

    private void addElectricFurnaceBridge() {
        var machineSet = AllBlockEntities.MACHINE_SETS.get("electric_furnace");
        if (machineSet == null) {
            return;
        }
        for (var voltage : machineSet.voltages) {
            var requirements = new ArrayList<IDependencyNode>();
            stackNode(new ItemStack(machineSet.block(voltage))).ifPresent(requirements::add);
            requirements.add(new VoltageNode(voltage));
            var output = new MachineNode(MINECRAFT_SMELTING, voltage);
            addMethodIfUseful("machine/" + output.id(), requirements, List.of(output), "electric furnace bridge");
        }
    }

    private void addMultiblockBridgeMethods() {
        for (var entry : AllMultiblocks.MULTIBLOCK_SETS.entrySet()) {
            var multiblockId = new ResourceLocation(TinactoryKeys.ID, entry.getKey());
            var set = entry.getValue();
            var requirements = new ArrayList<IDependencyNode>();
            stackNode(new ItemStack(set.block().get())).ifPresent(requirements::add);
            for (var i = 0; i < set.structureIngredients().size(); i++) {
                blockIngredientNode(set.structureIngredients().get(i), multiblockId, i).ifPresent(requirements::add);
            }
            var multiblock = new MultiblockNode(multiblockId);
            addMethodIfUseful("multiblock/" + multiblock.id(), requirements, List.of(multiblock), "multiblock bridge");
            for (var type : set.types()) {
                addMultiblockMachineBridge(multiblock, type.loc());
            }
            if (multiblockId.equals(MULTI_SMELTER)) {
                addMultiblockMachineBridge(multiblock, MINECRAFT_SMELTING);
            }
        }
    }

    private void addMultiblockMachineBridge(MultiblockNode multiblock, ResourceLocation recipeTypeId) {
        for (var voltage : Voltage.values()) {
            if (voltage == Voltage.PRIMITIVE || voltage == Voltage.MAX) {
                continue;
            }
            var output = new MachineNode(recipeTypeId, voltage);
            var requirements = new ArrayList<IDependencyNode>();
            requirements.add(multiblock);
            if (!isGeneratorRecipeType(recipeTypeId)) {
                requirements.add(new VoltageNode(voltage));
            }
            for (var machineInterface : multiblockInterfaceNodes(voltage)) {
                addMethod(new DependencyMethod(
                    "multiblock_machine/" + multiblock.id() + "/" + output.id() + "/" + machineInterface.id(),
                    with(requirements, machineInterface), List.of(output),
                    "multiblock machine bridge"));
            }
        }
    }

    private void addBoilerBridgeMethods() {
        var output = new MachineNode(BOILER, Voltage.PRIMITIVE);
        for (var block : BOILER_BLOCKS) {
            itemNode(block).ifPresent(boiler -> addMethod(new DependencyMethod(
                "boiler/" + block, List.of(boiler), List.of(output), "boiler bridge")));
        }
        for (var multiblockId : BOILER_MULTIBLOCKS) {
            var multiblock = new MultiblockNode(multiblockId);
            for (var voltage : Voltage.values()) {
                if (voltage == Voltage.PRIMITIVE || voltage == Voltage.MAX) {
                    continue;
                }
                for (var machineInterface : multiblockInterfaceNodes(voltage)) {
                    addMethod(new DependencyMethod(
                        "boiler/" + multiblockId + "/" + voltage.id + "/" + machineInterface.id(),
                        List.of(multiblock, machineInterface), List.of(output), "multiblock boiler bridge"));
                }
            }
        }
    }

    private void addVoltageBridgeMethods() {
        for (var voltage : Voltage.values()) {
            if (voltage == Voltage.PRIMITIVE || voltage == Voltage.MAX) {
                continue;
            }
            cableNode(voltage).ifPresent(cable -> addMethod(new DependencyMethod(
                "voltage/generator/" + voltage.id,
                List.of(cable, new GeneratorNode(voltage)), List.of(new VoltageNode(voltage)),
                "voltage generator bridge")));
            transformerNode(voltage).ifPresent(transformer -> {
                if (voltage.rank > Voltage.ULV.rank) {
                    cableNode(voltage).ifPresent(cable -> addMethod(new DependencyMethod(
                        "voltage/transformer/" + voltage.id,
                        List.of(new VoltageNode(Voltage.fromRank(voltage.rank - 1)), transformer, cable),
                        List.of(new VoltageNode(voltage)), "voltage transformer bridge")));
                }
            });
        }
    }

    private void addCoilBridgeMethods() {
        for (var entry : AllMultiblocks.COIL_BLOCKS.values()) {
            var block = entry.get();
            var requirements = new ArrayList<IDependencyNode>();
            stackNode(new ItemStack(block)).ifPresent(requirements::add);
            addMethodIfUseful(
                "coil/" + block.temperature, requirements,
                List.of(new NumericNode(COIL_TEMPERATURE, block.temperature)), "coil bridge");
        }
    }

    private void addCleanroomBridgeMethods() {
        for (var entry : AllMultiblocks.CLEANROOM_PROPERTIES.entrySet()) {
            var multiblock = new MultiblockNode(new ResourceLocation(TinactoryKeys.ID, entry.getKey()));
            for (var voltage : Voltage.values()) {
                if (voltage == Voltage.PRIMITIVE || voltage == Voltage.MAX) {
                    continue;
                }
                var cleanness = maxCleanroomCleanness(entry.getValue(), voltage);
                var output = new NumericNode(CLEANROOM_CLEANNESS, cleanness);
                for (var machineInterface : multiblockInterfaceNodes(voltage)) {
                    addMethod(new DependencyMethod(
                        "cleanroom/cleanness/" + multiblock.id() + "/" + voltage.id + "/" + machineInterface.id(),
                        List.of(multiblock, new VoltageNode(voltage), machineInterface),
                        List.of(output), "cleanroom bridge"));
                }
            }
        }
    }

    private static double maxCleanroomCleanness(Cleanroom.Properties properties, Voltage voltage) {
        var clean = properties.baseClean() * Math.sqrt((double) voltage.value / (double) Voltage.ULV.value);
        var decay = properties.baseDecay();
        return (1d - decay) * clean / (clean + decay - clean * decay);
    }

    private void addNuclearReactorBridgeMethods() {
        for (var item : ForgeRegistries.ITEMS) {
            if (item instanceof NuclearRod nuclearRod) {
                var requirements = new ArrayList<IDependencyNode>();
                stackNode(new ItemStack(item)).ifPresent(requirements::add);
                requirements.add(new MultiblockNode(NUCLEAR_REACTOR));
                var outputs = new ArrayList<IDependencyNode>();
                stackNode(nuclearRod.getDepleted()).ifPresent(outputs::add);
                var itemId = ForgeRegistries.ITEMS.getKey(item);
                addMethodIfUseful("nuclear_reactor/deplete/" + itemId, requirements, outputs,
                    "nuclear reactor depletion bridge");
            }
        }
    }

    private void addTagBridgeMethods() {
        var tagNodes = new TreeSet<TagNode>();
        for (var method : methods) {
            method.requirements().stream()
                .filter(TagNode.class::isInstance)
                .map(TagNode.class::cast)
                .forEach(tagNodes::add);
        }
        for (var tagNode : tagNodes) {
            if (tagNode.portType() == PortType.ITEM) {
                addItemTagBridgeMethods(tagNode);
            }
        }
    }

    private Collection<LithographyLensBlock> lithographyLensBlocks() {
        if (lithographyLensBlocks == null) {
            var ret = new ArrayList<LithographyLensBlock>();
            for (var block : ForgeRegistries.BLOCKS) {
                if (block instanceof LensBlock lensBlock) {
                    stackNode(new ItemStack(block))
                        .ifPresent(node -> ret.add(new LithographyLensBlock(node, lensBlock.getLens())));
                }
            }
            lithographyLensBlocks = ret;
        }
        return lithographyLensBlocks;
    }

    private static boolean matchLens(Collection<Item> lens, IProcessingIngredient ingredient) {
        if (ingredient instanceof ItemsIngredient items) {
            return lens.stream().anyMatch($ -> items.ingredient.test(new ItemStack($)));
        } else if (ingredient instanceof StackIngredient<?> item && item.type() == PortType.ITEM) {
            return lens.stream().anyMatch($ -> ((ItemStack) item.stack()).is($));
        } else {
            return false;
        }
    }

    private void addItemTagBridgeMethods(TagNode tagNode) {
        var tag = TagKey.create(Registry.ITEM_REGISTRY, tagNode.tagId());
        for (var item : ForgeRegistries.ITEMS.tags().getTag(tag)) {
            var requirements = new ArrayList<IDependencyNode>();
            stackNode(new ItemStack(item)).ifPresent(requirements::add);
            addMethodIfUseful("tag/" + tagNode.id() + "/" + ForgeRegistries.ITEMS.getKey(item),
                requirements, List.of(tagNode), "item tag bridge");
        }
    }

    private Collection<IDependencyNode> startNodes() {
        var ret = new ArrayList<IDependencyNode>();
        ret.add(new VoltageNode(Voltage.PRIMITIVE));
        for (var item : List.of(Items.OAK_LOG, Items.WATER_BUCKET)) {
            stackNode(new ItemStack(item)).ifPresent(ret::add);
        }
        stackNode(new ItemStack(AllItems.RUBBER_SAPLING.get())).ifPresent(ret::add);
        itemNode(STICKY_RESIN).ifPresent(ret::add);
        stackNode(new FluidStack(Fluids.WATER, 1000)).ifPresent(ret::add);
        return ret;
    }

    private Set<IDependencyNode> intendedTargets() {
        var ret = new TreeSet<IDependencyNode>();
        addMaterialTargets(ret);
        addTinactoryItemTargets(ret);
        addMachineTargets(ret);
        addMultiblockTargets(ret);
        ret.add(new VoltageNode(MAX_PROGRESS_VOLTAGE));
        TechManagers.server().allTechs().stream()
            .map(technology -> new TechnologyNode(technology.loc()))
            .forEach(ret::add);
        return ret;
    }

    private void addMaterialTargets(Collection<IDependencyNode> targets) {
        for (var material : AllMaterials.SET.values()) {
            for (var sub : material.itemSubs()) {
                if (!material.isAlias(sub)) {
                    stackNode(new ItemStack(material.item(sub))).ifPresent(targets::add);
                }
            }
        }
    }

    private void addTinactoryItemTargets(Collection<IDependencyNode> targets) {
        for (var item : ForgeRegistries.ITEMS) {
            var key = ForgeRegistries.ITEMS.getKey(item);
            if (key != null && key.getNamespace().equals(TinactoryKeys.ID)) {
                stackNode(new ItemStack(item)).ifPresent(targets::add);
            }
        }
    }

    private void addMachineTargets(Collection<IDependencyNode> targets) {
        for (var machineSet : AllBlockEntities.MACHINE_SETS.values()) {
            if (machineSet instanceof ProcessingSet processingSet &&
                processingSet.hasVoltage(MAX_PROGRESS_VOLTAGE)) {
                targets.add(new MachineNode(processingSet.recipeType.loc(), MAX_PROGRESS_VOLTAGE));
            }
        }
        targets.add(new MachineNode(MINECRAFT_SMELTING, MAX_PROGRESS_VOLTAGE));
        for (var set : AllMultiblocks.MULTIBLOCK_SETS.values()) {
            for (var type : set.types()) {
                targets.add(new MachineNode(type.loc(), MAX_PROGRESS_VOLTAGE));
            }
        }
    }

    private void addMultiblockTargets(Collection<IDependencyNode> targets) {
        for (var entry : AllMultiblocks.MULTIBLOCK_SETS.entrySet()) {
            var multiblockId = new ResourceLocation(TinactoryKeys.ID, entry.getKey());
            targets.add(new MultiblockNode(multiblockId));
        }
    }

    private Map<IDependencyNode, String> exemptTargets() {
        var ret = new HashMap<IDependencyNode, String>();
        addMaterialExemptions(ret, TEST_MATERIAL, "test-only material set");
        return ret;
    }

    private void addMaterialExemptions(Map<IDependencyNode, String> exemptions, String materialName, String reason) {
        var material = AllMaterials.getMaterial(materialName);
        for (var sub : material.itemSubs()) {
            if (!material.isAlias(sub)) {
                stackNode(new ItemStack(material.item(sub))).ifPresent(node -> exemptions.put(node, reason));
            }
        }
    }

    private boolean writeReport(IDependencySolver solver, Collection<IDependencyNode> startNodes,
        Set<IDependencyNode> targets, Map<IDependencyNode, String> exemptions) {
        var reportFile = System.getProperty("tinactory.dependencyChecker.reportFile", "");
        var lines = new ArrayList<String>();
        var verbose = Boolean.getBoolean("tinactory.dependencyChecker.verbose");
        if (verbose) {
            lines.add("start nodes:");
            startNodes.stream()
                .map(node -> "  " + node.displayId())
                .sorted()
                .forEach(lines::add);
            lines.add("");
        }
        var missingTargets = 0;
        for (var target : targets) {
            if (!target.isSatisfied(solver) && !exemptions.containsKey(target)) {
                missingTargets++;
                lines.addAll(formatMissingTarget(solver, target, verbose));
            }
        }
        if (!reportFile.isBlank()) {
            var path = Path.of(reportFile);
            try {
                var parent = path.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.write(path, lines);
                LOGGER.info("Tinactory dependency checker report: {}", path);
            } catch (Exception e) {
                LOGGER.warn("Failed to write Tinactory dependency checker report {}", path, e);
            }
        }
        if (missingTargets > 0) {
            LOGGER.info("{}/{} nodes unreachable",
                missingTargets, targets.size());
        }
        if (missingTargets != ACCEPTED_UNREACHABLE_NODES) {
            LOGGER.error("differs from baseline: {}", ACCEPTED_UNREACHABLE_NODES);
            return false;
        }
        return true;
    }

    private Collection<String> formatMissingTarget(IDependencySolver solver, IDependencyNode target, boolean verbose) {
        var lines = new ArrayList<String>();
        lines.add("missing: " + target.displayId());
        if (verbose) {
            for (var method : methodsByOutput.getOrDefault(target, Set.of())) {
                lines.add("  blocked method: " + method.id() + " (" + method.source() + ")");
                method.requirements().stream()
                    .filter(requirement -> !requirement.isSatisfied(solver))
                    .map(requirement -> "    missing requirement: " + requirement.displayId())
                    .forEach(lines::add);
            }
        }
        return lines;
    }

    private Optional<IDependencyNode> ingredientNode(IProcessingIngredient ingredient, ResourceLocation recipeId,
        int inputIndex) {
        if (ingredient instanceof TagIngredient tagIngredient) {
            return Optional.of(new TagNode(PortType.ITEM, tagIngredient.tag().location()));
        }
        if (ingredient instanceof ItemsIngredient itemsIngredient) {
            return ingredientNode(itemsIngredient.ingredient, recipeId, inputIndex);
        }
        return ProcessingHelper.asItemIngredient(ingredient)
            .flatMap(stackIngredient -> stackNode(stackIngredient.stack()))
            .map(node -> (IDependencyNode) node)
            .or(() -> ProcessingHelper.asFluidIngredient(ingredient)
                .flatMap(stackIngredient -> stackNode(stackIngredient.stack())));
    }

    private Optional<IDependencyNode> resultNode(IProcessingResult result) {
        return ProcessingHelper.asItemResult(result)
            .filter(stackResult -> stackResult.rate() > 0d)
            .flatMap(stackResult -> stackNode(stackResult.stack()))
            .map(node -> (IDependencyNode) node)
            .or(() -> ProcessingHelper.asFluidResult(result)
                .filter(stackResult -> stackResult.rate() > 0d)
                .flatMap(stackResult -> stackNode(stackResult.stack())));
    }

    private Optional<IDependencyNode> ingredientNode(Ingredient ingredient, ResourceLocation recipeId, int inputIndex) {
        var json = ingredient.toJson();
        if (json.isJsonObject()) {
            var object = json.getAsJsonObject();
            if (object.has("tag")) {
                return Optional.of(new TagNode(PortType.ITEM, new ResourceLocation(object.get("tag").getAsString())));
            }
            if (object.has("item")) {
                var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(object.get("item").getAsString()));
                if (item != null) {
                    return stackNode(new ItemStack(item)).map(node -> node);
                }
            }
        }
        var items = ingredient.getItems();
        if (items.length == 0) {
            return Optional.empty();
        }
        if (items.length == 1) {
            return stackNode(items[0]).map(node -> node);
        }
        var node = new IngredientNode("recipe", recipeId, inputIndex);
        addIngredientBridgeMethods(node, List.of(items));
        return Optional.of(node);
    }

    private Optional<IDependencyNode> blockIngredientNode(BlockIngredient ingredient, ResourceLocation multiblockId,
        int inputIndex) {
        var candidates = ingredient.expand().stream()
            .map(ItemStack::new)
            .filter(stack -> !stack.isEmpty())
            .toList();
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        if (candidates.size() == 1) {
            return stackNode(candidates.get(0)).map(node -> node);
        }
        var node = new IngredientNode("multiblock", multiblockId, inputIndex);
        addIngredientBridgeMethods(node, candidates);
        return Optional.of(node);
    }

    private void addIngredientBridgeMethods(IngredientNode node, Collection<ItemStack> candidates) {
        for (var candidate : candidates) {
            stackNode(candidate).ifPresent(candidateNode -> addMethodIfUseful(
                "ingredient/" + node.id() + "/" + candidateNode.id(),
                List.of(candidateNode), List.of(node), "ingredient bridge"));
        }
    }

    private Optional<StackNode> cableNode(Voltage voltage) {
        return componentNode("cable", voltage);
    }

    private Optional<StackNode> transformerNode(Voltage voltage) {
        return componentNode("transformer", voltage);
    }

    private Collection<StackNode> multiblockInterfaceNodes(Voltage voltage) {
        var ret = new ArrayList<StackNode>();
        for (var name : List.of("multiblock/interface", "multiblock/digital_interface")) {
            var machineSet = AllBlockEntities.MACHINE_SETS.get(name);
            if (machineSet != null && machineSet.hasVoltage(voltage)) {
                stackNode(new ItemStack(machineSet.block(voltage))).ifPresent(ret::add);
            }
        }
        return ret;
    }

    private Optional<StackNode> componentNode(String name, Voltage voltage) {
        var entries = AllItems.COMPONENTS.get(name);
        if (entries == null || !entries.containsKey(voltage)) {
            return Optional.empty();
        }
        return stackNode(new ItemStack(entries.get(voltage).get()));
    }

    private static boolean isGeneratorRecipeType(ResourceLocation recipeTypeId) {
        return GENERATOR_RECIPE_TYPES.contains(recipeTypeId);
    }

    private static <T> List<T> with(Collection<T> values, T value) {
        var ret = new ArrayList<>(values);
        ret.add(value);
        return ret;
    }

    private Optional<StackNode> itemNode(ResourceLocation loc) {
        var item = ForgeRegistries.ITEMS.getValue(loc);
        return item == null ? Optional.empty() : stackNode(new ItemStack(item));
    }

    private Optional<StackNode> stackNode(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.of(stackNode(StackHelper.ITEM_ADAPTER.keyOf(stack)));
    }

    private Optional<StackNode> stackNode(FluidStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.of(stackNode(StackHelper.FLUID_ADAPTER.keyOf(stack)));
    }

    private StackNode stackNode(IStackKey key) {
        return new StackNode(key);
    }

    private void addMethodIfUseful(String id, Collection<IDependencyNode> requirements,
        Collection<IDependencyNode> outputs, String source) {
        if (!outputs.isEmpty()) {
            addMethod(new DependencyMethod(id, requirements, outputs, source));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Collection<? extends IEntry<? extends IRecipe<?>>> allTinyRecipeEntries(IRecipeManager recipeManager,
        IRecipeType<?> recipeType) {
        return recipeManager.getAllRecipesFor((IRecipeType) recipeType);
    }
}
