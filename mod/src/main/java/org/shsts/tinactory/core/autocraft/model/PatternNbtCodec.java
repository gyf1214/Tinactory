package org.shsts.tinactory.core.autocraft.model;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraintRegistry;

import java.util.ArrayList;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;
import static org.shsts.tinactory.core.autocraft.model.CraftKey.Type.FLUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PatternNbtCodec {
    private final IMachineConstraintRegistry constraints;

    public PatternNbtCodec(IMachineConstraintRegistry constraints) {
        this.constraints = constraints;
    }

    public CompoundTag encodePattern(CraftPattern pattern) {
        var tag = new CompoundTag();
        tag.putString("patternId", pattern.patternId());
        tag.put("inputs", encodeAmounts(pattern.inputs()));
        tag.put("outputs", encodeAmounts(pattern.outputs()));
        tag.put("machineRequirement", encodeMachineRequirement(pattern.machineRequirement()));
        return tag;
    }

    public CraftPattern decodePattern(CompoundTag tag) {
        return new CraftPattern(
            tag.getString("patternId"),
            decodeAmounts(tag.getList("inputs", TAG_COMPOUND)),
            decodeAmounts(tag.getList("outputs", TAG_COMPOUND)),
            decodeMachineRequirement(tag.getCompound("machineRequirement")));
    }

    private ListTag encodeAmounts(Iterable<CraftAmount> amounts) {
        var list = new ListTag();
        for (var amount : amounts) {
            list.add(encodeAmount(amount));
        }
        return list;
    }

    private CraftAmount decodeAmount(CompoundTag tag) {
        var type = CraftKey.Type.valueOf(tag.getString("type"));
        var key = type == FLUID ?
            CraftKey.fluid(tag.getString("id"), tag.getString("nbt")) :
            CraftKey.item(tag.getString("id"), tag.getString("nbt"));
        return new CraftAmount(key, tag.getLong("amount"));
    }

    private CompoundTag encodeAmount(CraftAmount amount) {
        var tag = new CompoundTag();
        tag.putString("type", amount.key().type().name());
        tag.putString("id", amount.key().id());
        tag.putString("nbt", amount.key().nbt());
        tag.putLong("amount", amount.amount());
        return tag;
    }

    private ArrayList<CraftAmount> decodeAmounts(ListTag list) {
        var ret = new ArrayList<CraftAmount>(list.size());
        for (var i = 0; i < list.size(); i++) {
            ret.add(decodeAmount(list.getCompound(i)));
        }
        return ret;
    }

    private CompoundTag encodeMachineRequirement(MachineRequirement requirement) {
        var tag = new CompoundTag();
        tag.putString("recipeTypeId", requirement.recipeTypeId().toString());
        tag.putInt("voltageTier", requirement.voltageTier());

        var constraintsTag = new ListTag();
        for (var constraint : requirement.constraints()) {
            var encoded = constraints.encode(constraint);
            var entry = new CompoundTag();
            entry.putString("typeId", encoded.typeId());
            entry.putString("payload", encoded.payload());
            constraintsTag.add(entry);
        }
        tag.put("constraints", constraintsTag);
        return tag;
    }

    private MachineRequirement decodeMachineRequirement(CompoundTag tag) {
        var constraintsTag = tag.getList("constraints", TAG_COMPOUND);
        var constraintsOut = new ArrayList<IMachineConstraint>(constraintsTag.size());
        for (var i = 0; i < constraintsTag.size(); i++) {
            var entry = constraintsTag.getCompound(i);
            constraintsOut.add(constraints.decode(entry.getString("typeId"), entry.getString("payload")));
        }
        return new MachineRequirement(
            new ResourceLocation(tag.getString("recipeTypeId")),
            tag.getInt("voltageTier"),
            constraintsOut);
    }
}
