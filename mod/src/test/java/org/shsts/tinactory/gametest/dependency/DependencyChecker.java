package org.shsts.tinactory.gametest.dependency;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
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
import org.shsts.tinactory.AllMultiblocks;
import org.shsts.tinactory.AllRecipes;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe;
import org.shsts.tinactory.content.recipe.CleanRecipe;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.recipe.ItemsIngredient;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;
import org.shsts.tinactory.integration.recipe.TagIngredient;
import org.shsts.tinactory.integration.tech.TechManagers;
import org.shsts.tinycorelib.api.recipe.IRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeManager;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.shsts.tinactory.Tinactory.CORE;

public final class DependencyChecker implements IDependencyChecker {
    private static final ResourceLocation LARGE_CHEMICAL_REACTOR = new ResourceLocation(
        TinactoryKeys.ID, "large_chemical_reactor");
    private static final ResourceLocation MINECRAFT_SMELTING = new ResourceLocation("minecraft", "smelting");
    private static final ResourceLocation MULTI_SMELTER = new ResourceLocation(TinactoryKeys.ID, "multi_smelter");
    private static final ResourceLocation STICKY_RESIN = new ResourceLocation(TinactoryKeys.ID,
        "rubber_tree/sticky_resin");
    private static final ResourceLocation TOOL_CRAFTING = new ResourceLocation(TinactoryKeys.ID, "tool_crafting");
    private static final String COIL_TEMPERATURE = "coil_temperature";
    private static final String CLEANROOM_CLEANNESS = "cleanroom_cleanness";

    private final List<DependencyMethod> methods = new ArrayList<>();
    private final Queue<DependencyMethod> readyMethods = new PriorityQueue<>();
    private final Queue<IDependencyNode> newlyReached = new PriorityQueue<>();
    private final Set<DependencyMethod> appliedMethods = new HashSet<>();
    private final Map<DependencyMethod, Integer> remainingRequirements = new HashMap<>();
    private final Map<IDependencyNode, Set<DependencyMethod>> waitingByExactNode = new HashMap<>();
    private final Map<String, NavigableMap<Double, Set<DependencyMethod>>> waitingByNumericNode = new HashMap<>();
    private final NavigableMap<Integer, Set<DependencyMethod>> waitingByVoltage = new TreeMap<>();
    private final Set<IDependencyNode> reachedExactNodes = new TreeSet<>();
    private final Map<IDependencyNode, DependencyMethod> firstReachingMethods = new HashMap<>();
    private final Map<String, Double> maxNumericNodes = new HashMap<>();
    private final Map<String, DependencyMethod> firstReachingNumericMethods = new HashMap<>();
    private final Map<Voltage, DependencyMethod> firstReachingVoltageMethods = new HashMap<>();
    private Voltage maxReachedVoltage = Voltage.PRIMITIVE;

    public void addMethod(DependencyMethod method) {
        methods.add(method);
    }

    public boolean isReached(IDependencyNode node) {
        return node.isSatisfied(this);
    }

    public int methodCount() {
        return methods.size();
    }

    public void solve(Collection<IDependencyNode> startNodes) {
        resetSolver();
        for (var node : startNodes) {
            node.reach(this, bootstrapMethod());
        }
        initializeMethods();
        while (!readyMethods.isEmpty() || !newlyReached.isEmpty()) {
            applyReadyMethods();
            releaseReachedNodes();
        }
    }

    public static void runSelfCheck() {
        var checker = new DependencyChecker();
        var seed = technology("seed");
        var exact = technology("exact");
        var numeric = technology("numeric");
        var voltage = technology("voltage");
        var duplicate = technology("duplicate");
        checker.addMethod(new DependencyMethod(
            "self/exact", List.of(seed), List.of(exact), "self check exact"));
        checker.addMethod(new DependencyMethod(
            "self/numeric", List.of(new NumericNode("temperature", 2d)), List.of(numeric), "self check numeric"));
        checker.addMethod(new DependencyMethod(
            "self/voltage", List.of(new VoltageNode(Voltage.MV)), List.of(voltage), "self check voltage"));
        checker.addMethod(new DependencyMethod(
            "self/duplicate", List.of(exact, exact), List.of(duplicate), "self check duplicate"));
        checker.solve(List.of(seed, new NumericNode("temperature", 3d), new VoltageNode(Voltage.HV)));
        checker.requireReached(exact);
        checker.requireReached(numeric);
        checker.requireReached(voltage);
        checker.requireReached(duplicate);
    }

    public static void runRuntimeCheck(ServerLevel world) {
        var checker = new DependencyChecker();
        checker.extractRuntimeMethods(world);
        checker.solve(checker.startNodes());
        checker.writeReport(checker.intendedTargets(), checker.exemptTargets());
    }

    @Override
    public boolean isExactReached(IDependencyNode node) {
        return reachedExactNodes.contains(node);
    }

    @Override
    public boolean reachExact(IDependencyNode node, DependencyMethod method) {
        if (!reachedExactNodes.add(node)) {
            return false;
        }
        firstReachingMethods.put(node, method);
        newlyReached.add(node);
        return true;
    }

    @Override
    public void addExactWaiter(IDependencyNode node, DependencyMethod method) {
        waitingByExactNode.computeIfAbsent(node, $ -> new TreeSet<>()).add(method);
    }

    @Override
    public Set<DependencyMethod> releaseExactWaiters(IDependencyNode node) {
        return waitingByExactNode.getOrDefault(node, Set.of());
    }

    @Override
    public double maxNumeric(String key) {
        return maxNumericNodes.getOrDefault(key, Double.NEGATIVE_INFINITY);
    }

    @Override
    public boolean reachNumeric(String key, double value, DependencyMethod method) {
        var oldValue = maxNumeric(key);
        if (oldValue >= value) {
            return false;
        }
        maxNumericNodes.put(key, value);
        firstReachingNumericMethods.put(key, method);
        newlyReached.add(new NumericNode(key, value));
        return true;
    }

    @Override
    public void addNumericWaiter(NumericNode node, DependencyMethod method) {
        waitingByNumericNode.computeIfAbsent(node.key(), $ -> new TreeMap<>())
            .computeIfAbsent(node.value(), $ -> new TreeSet<>())
            .add(method);
    }

    @Override
    public Set<DependencyMethod> releaseNumericWaiters(NumericNode node) {
        var methodsByValue = waitingByNumericNode.get(node.key());
        if (methodsByValue == null) {
            return Set.of();
        }
        var released = new TreeSet<DependencyMethod>();
        var reachedValues = new TreeMap<>(methodsByValue.headMap(node.value(), true));
        for (var methodSet : reachedValues.values()) {
            released.addAll(methodSet);
        }
        reachedValues.keySet().forEach(methodsByValue::remove);
        return released;
    }

    @Override
    public Voltage maxVoltage() {
        return maxReachedVoltage;
    }

    @Override
    public boolean reachVoltage(Voltage voltage, DependencyMethod method) {
        if (maxReachedVoltage.rank >= voltage.rank) {
            return false;
        }
        maxReachedVoltage = voltage;
        firstReachingVoltageMethods.put(voltage, method);
        newlyReached.add(new VoltageNode(voltage));
        return true;
    }

    @Override
    public void addVoltageWaiter(VoltageNode node, DependencyMethod method) {
        waitingByVoltage.computeIfAbsent(node.voltage().rank, $ -> new TreeSet<>()).add(method);
    }

    @Override
    public Set<DependencyMethod> releaseVoltageWaiters(VoltageNode node) {
        var released = new TreeSet<DependencyMethod>();
        var reachedRanks = new TreeMap<>(waitingByVoltage.headMap(node.voltage().rank, true));
        for (var methodSet : reachedRanks.values()) {
            released.addAll(methodSet);
        }
        reachedRanks.keySet().forEach(waitingByVoltage::remove);
        return released;
    }

    private void resetSolver() {
        readyMethods.clear();
        newlyReached.clear();
        appliedMethods.clear();
        remainingRequirements.clear();
        waitingByExactNode.clear();
        waitingByNumericNode.clear();
        waitingByVoltage.clear();
        reachedExactNodes.clear();
        firstReachingMethods.clear();
        maxNumericNodes.clear();
        firstReachingNumericMethods.clear();
        firstReachingVoltageMethods.clear();
        maxReachedVoltage = Voltage.PRIMITIVE;
    }

    private void initializeMethods() {
        for (var method : new TreeSet<>(methods)) {
            var remaining = 0;
            for (var requirement : method.requirements()) {
                if (!requirement.isSatisfied(this)) {
                    remaining++;
                    requirement.addWaiter(this, method);
                }
            }
            remainingRequirements.put(method, remaining);
            if (remaining == 0) {
                readyMethods.add(method);
            }
        }
    }

    private void applyReadyMethods() {
        while (!readyMethods.isEmpty()) {
            var method = readyMethods.remove();
            if (!appliedMethods.add(method)) {
                continue;
            }
            for (var output : method.outputs()) {
                output.reach(this, method);
            }
        }
    }

    private void releaseReachedNodes() {
        while (!newlyReached.isEmpty()) {
            var node = newlyReached.remove();
            for (var method : node.releaseWaiters(this)) {
                var remaining = remainingRequirements.merge(method, -1, Integer::sum);
                if (remaining == 0) {
                    readyMethods.add(method);
                }
            }
        }
    }

    private void requireReached(IDependencyNode node) {
        if (!isReached(node)) {
            throw new AssertionError("Expected dependency node to be reached: " + node.displayId());
        }
    }

    private void extractRuntimeMethods(ServerLevel world) {
        extractProcessingRecipes(world);
        extractVanillaRecipes(world);
        addMachineBridgeMethods();
        addWorkbenchBridgeMethod();
        addMultiblockBridgeMethods();
        addVoltageBridgeMethods();
        addCoilBridgeMethods();
        addCleanroomBridgeMethods();
        addTagBridgeMethods();
    }

    private void extractProcessingRecipes(ServerLevel world) {
        var recipeManager = CORE.recipeManager(world);
        for (var typeInfo : AllRecipes.PROCESSING_TYPES.values()) {
            for (var recipe : allTinyRecipes(recipeManager, typeInfo.recipeType())) {
                if (recipe instanceof ProcessingRecipe processingRecipe) {
                    addProcessingRecipeMethod(typeInfo.recipeType().loc(), processingRecipe);
                }
            }
        }
        for (var recipe : recipeManager.getAllRecipesFor(AllRecipes.BOILER)) {
            var requirements = new ArrayList<IDependencyNode>();
            stackNode(recipe.input).ifPresent(requirements::add);
            var outputs = new ArrayList<IDependencyNode>();
            stackNode(recipe.output).ifPresent(outputs::add);
            addMethodIfUseful(recipe.loc() + "#boiler", requirements, outputs, "boiler recipe " + recipe.loc());
        }
    }

    private void addProcessingRecipeMethod(ResourceLocation recipeTypeId, ProcessingRecipe recipe) {
        var requirements = new ArrayList<IDependencyNode>();
        requirements.add(new MachineNode(recipeTypeId, Voltage.fromValue(recipe.voltage)));
        requirements.add(new VoltageNode(Voltage.fromValue(recipe.voltage)));
        recipe.inputs.stream()
            .map(ProcessingRecipe.Input::ingredient)
            .map(this::ingredientNode)
            .flatMap(Optional::stream)
            .forEach(requirements::add);
        addProcessingSubtypeRequirements(recipe, requirements);
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
        addMethodIfUseful(recipe.loc() + "#processing", requirements, outputs, "processing recipe " + recipe.loc());
    }

    private void addProcessingSubtypeRequirements(ProcessingRecipe recipe, Collection<IDependencyNode> requirements) {
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
        if (recipe instanceof CleanRecipe cleanRecipe && cleanRecipe.minCleanness > 0d) {
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
        for (var recipe : CORE.recipeManager(world).getAllRecipesFor(AllRecipes.TOOL_CRAFTING)) {
            addToolRecipeMethod(recipe);
        }
    }

    private void addSmeltingRecipeMethod(SmeltingRecipe recipe) {
        var requirements = new ArrayList<IDependencyNode>();
        ingredientNode(recipe.getIngredients().get(0), recipe.getId()).ifPresent(requirements::add);
        requirements.add(new MachineNode(MINECRAFT_SMELTING, Voltage.PRIMITIVE));
        var outputs = new ArrayList<IDependencyNode>();
        stackNode(recipe.getResultItem()).ifPresent(outputs::add);
        addMethodIfUseful(recipe.getId() + "#smelting", requirements, outputs, "smelting recipe " + recipe.getId());
    }

    private void addCraftingRecipeMethod(CraftingRecipe recipe) {
        var requirements = new ArrayList<IDependencyNode>();
        for (var ingredient : recipe.getIngredients()) {
            if (!ingredient.isEmpty()) {
                ingredientNode(ingredient, recipe.getId()).ifPresent(requirements::add);
            }
        }
        var outputs = new ArrayList<IDependencyNode>();
        stackNode(recipe.getResultItem()).ifPresent(outputs::add);
        addMethodIfUseful(recipe.getId() + "#crafting", requirements, outputs, "crafting recipe " + recipe.getId());
    }

    private void addToolRecipeMethod(ToolRecipe recipe) {
        var requirements = new ArrayList<IDependencyNode>();
        requirements.add(new MachineNode(TOOL_CRAFTING, Voltage.PRIMITIVE));
        for (var ingredient : recipe.shapedRecipe.getIngredients()) {
            if (!ingredient.isEmpty()) {
                ingredientNode(ingredient, recipe.loc()).ifPresent(requirements::add);
            }
        }
        for (var ingredient : recipe.toolIngredients) {
            if (!ingredient.isEmpty()) {
                ingredientNode(ingredient, recipe.loc()).ifPresent(requirements::add);
            }
        }
        var outputs = new ArrayList<IDependencyNode>();
        stackNode(recipe.shapedRecipe.getResultItem()).ifPresent(outputs::add);
        addMethodIfUseful(recipe.loc() + "#tool_crafting", requirements, outputs,
            "tool crafting recipe " + recipe.loc());
    }

    private void addMachineBridgeMethods() {
        for (var machineSet : AllBlockEntities.MACHINE_SETS.values()) {
            if (machineSet instanceof ProcessingSet processingSet) {
                for (var voltage : processingSet.voltages) {
                    var requirements = new ArrayList<IDependencyNode>();
                    stackNode(new ItemStack(processingSet.block(voltage))).ifPresent(requirements::add);
                    var output = new MachineNode(processingSet.recipeType.loc(), voltage);
                    addMethodIfUseful("machine/" + output.id(), requirements, List.of(output), "machine bridge");
                }
            }
        }
        addElectricFurnaceBridge();
    }

    private void addWorkbenchBridgeMethod() {
        var requirements = new ArrayList<IDependencyNode>();
        stackNode(new ItemStack(AllBlockEntities.WORKBENCH.get())).ifPresent(requirements::add);
        var output = new MachineNode(TOOL_CRAFTING, Voltage.PRIMITIVE);
        addMethodIfUseful("machine/" + output.id(), requirements, List.of(output), "workbench bridge");
    }

    private void addElectricFurnaceBridge() {
        var machineSet = AllBlockEntities.MACHINE_SETS.get("electric_furnace");
        if (machineSet == null) {
            return;
        }
        for (var voltage : machineSet.voltages) {
            var requirements = new ArrayList<IDependencyNode>();
            stackNode(new ItemStack(machineSet.block(voltage))).ifPresent(requirements::add);
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
            addMethod(new DependencyMethod(
                "multiblock_machine/" + multiblock.id() + "/" + output.id(),
                List.of(multiblock, new VoltageNode(voltage)), List.of(output), "multiblock machine bridge"));
        }
    }

    private void addVoltageBridgeMethods() {
        for (var voltage : Voltage.values()) {
            if (voltage == Voltage.PRIMITIVE || voltage == Voltage.MAX) {
                continue;
            }
            cableNode(voltage).ifPresent(cable -> addMethod(new DependencyMethod(
                "voltage/cable/" + voltage.id, List.of(cable), List.of(new VoltageNode(voltage)),
                "voltage cable bridge")));
            transformerNode(voltage).ifPresent(transformer -> {
                if (voltage.rank > Voltage.ULV.rank) {
                    addMethod(new DependencyMethod(
                        "voltage/transformer/" + voltage.id,
                        List.of(new VoltageNode(Voltage.fromRank(voltage.rank - 1)), transformer),
                        List.of(new VoltageNode(voltage)), "voltage transformer bridge"));
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
        var cleanroom = new MultiblockNode(new ResourceLocation(TinactoryKeys.ID, "cleanroom"));
        addMethod(new DependencyMethod(
            "cleanroom/cleanness", List.of(cleanroom, new VoltageNode(Voltage.ULV)),
            List.of(new NumericNode(CLEANROOM_CLEANNESS, 1d)), "cleanroom bridge"));
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
        ret.add(new MachineNode(MINECRAFT_SMELTING, Voltage.PRIMITIVE));
        for (var item : List.of(
            Items.AIR, Items.COBBLESTONE, Items.DIRT, Items.GRAVEL, Items.SAND, Items.CLAY_BALL,
            Items.OAK_LOG, Items.STICK, Items.FLINT, Items.COAL, Items.CHARCOAL, Items.IRON_INGOT,
            Items.COPPER_INGOT)) {
            stackNode(new ItemStack(item)).ifPresent(ret::add);
        }
        itemNode(STICKY_RESIN).ifPresent(ret::add);
        stackNode(new FluidStack(Fluids.WATER, 1000)).ifPresent(ret::add);
        stackNode(new FluidStack(Fluids.LAVA, 1000)).ifPresent(ret::add);
        return ret;
    }

    private Set<IDependencyNode> intendedTargets() {
        var ret = new TreeSet<IDependencyNode>();
        for (var method : methods) {
            method.outputs().stream()
                .filter(output -> !(output instanceof TagNode))
                .forEach(ret::add);
        }
        for (var machineSet : AllBlockEntities.MACHINE_SETS.values()) {
            for (var voltage : machineSet.voltages) {
                stackNode(new ItemStack(machineSet.block(voltage))).ifPresent(ret::add);
            }
        }
        for (var multiblockSet : AllMultiblocks.MULTIBLOCK_SETS.values()) {
            stackNode(new ItemStack(multiblockSet.block().get())).ifPresent(ret::add);
        }
        for (var componentSet : AllItems.COMPONENTS.values()) {
            for (var entry : componentSet.values()) {
                stackNode(new ItemStack(entry.get())).ifPresent(ret::add);
            }
        }
        TechManagers.server().allTechs().stream()
            .map(technology -> new TechnologyNode(technology.loc()))
            .forEach(ret::add);
        return ret;
    }

    private Map<IDependencyNode, String> exemptTargets() {
        return Map.of();
    }

    private void writeReport(Set<IDependencyNode> targets, Map<IDependencyNode, String> exemptions) {
        var reportFile = System.getProperty("tinactory.dependencyChecker.reportFile", "");
        if (reportFile.isBlank()) {
            return;
        }
        var lines = new ArrayList<String>();
        var missingTargets = 0;
        for (var target : targets) {
            if (!target.isSatisfied(this) && !exemptions.containsKey(target)) {
                missingTargets++;
                lines.addAll(formatMissingTarget(target));
            }
        }
        var path = Path.of(reportFile);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, lines);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write dependency checker report " + path, e);
        }
        if (!lines.isEmpty()) {
            System.err.println("Tinactory dependency checker found " + missingTargets + "/" + targets.size() +
                " nodes unreachable: " + path);
        }
    }

    private Collection<String> formatMissingTarget(IDependencyNode target) {
        return List.of("missing: " + target.displayId());
    }

    private Optional<IDependencyNode> ingredientNode(IProcessingIngredient ingredient) {
        if (ingredient instanceof TagIngredient tagIngredient) {
            return Optional.of(new TagNode(PortType.ITEM, tagIngredient.tag().location()));
        }
        if (ingredient instanceof ItemsIngredient itemsIngredient) {
            return ingredientNode(itemsIngredient.ingredient, null);
        }
        return ProcessingHelper.asItemIngredient(ingredient)
            .flatMap(stackIngredient -> stackNode(stackIngredient.stack()))
            .map(node -> (IDependencyNode) node)
            .or(() -> ProcessingHelper.asFluidIngredient(ingredient)
                .flatMap(stackIngredient -> stackNode(stackIngredient.stack()))
                .map(node -> node));
    }

    private Optional<IDependencyNode> resultNode(IProcessingResult result) {
        return ProcessingHelper.asItemResult(result)
            .filter(stackResult -> stackResult.rate() > 0d)
            .flatMap(stackResult -> stackNode(stackResult.stack()))
            .map(node -> (IDependencyNode) node)
            .or(() -> ProcessingHelper.asFluidResult(result)
                .filter(stackResult -> stackResult.rate() > 0d)
                .flatMap(stackResult -> stackNode(stackResult.stack()))
                .map(node -> node));
    }

    private Optional<IDependencyNode> ingredientNode(Ingredient ingredient, ResourceLocation recipeId) {
        var json = ingredient.toJson();
        if (json.isJsonObject()) {
            var object = json.getAsJsonObject();
            if (object.has("tag")) {
                return Optional.of(new TagNode(PortType.ITEM, new ResourceLocation(object.get("tag").getAsString())));
            }
            if (object.has("item")) {
                var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(object.get("item").getAsString()));
                if (item != null) {
                    return stackNode(new ItemStack(item)).map(node -> (IDependencyNode) node);
                }
            }
        }
        var items = ingredient.getItems();
        if (items.length != 1) {
            System.err.println("Tinactory dependency checker found non-tag ingredient with " + items.length +
                " item alternatives" + (recipeId == null ? "" : " in recipe " + recipeId));
        }
        return items.length == 0 ? Optional.empty() :
            stackNode(items[0]).map(node -> (IDependencyNode) node);
    }

    private Optional<StackNode> cableNode(Voltage voltage) {
        return componentNode("cable", voltage);
    }

    private Optional<StackNode> transformerNode(Voltage voltage) {
        return componentNode("transformer", voltage);
    }

    private Optional<StackNode> componentNode(String name, Voltage voltage) {
        var entries = AllItems.COMPONENTS.get(name);
        if (entries == null || !entries.containsKey(voltage)) {
            return Optional.empty();
        }
        return stackNode(new ItemStack(entries.get(voltage).get()));
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
    private static Collection<? extends IRecipe<?>> allTinyRecipes(IRecipeManager recipeManager,
        IRecipeType<?> recipeType) {
        return recipeManager.getAllRecipesFor((IRecipeType) recipeType);
    }

    private static DependencyMethod bootstrapMethod() {
        return new DependencyMethod("self/bootstrap", List.of(), List.of(), "self check bootstrap");
    }

    private static TechnologyNode technology(String id) {
        return new TechnologyNode(new ResourceLocation(TinactoryKeys.ID, "self_check/" + id));
    }
}
