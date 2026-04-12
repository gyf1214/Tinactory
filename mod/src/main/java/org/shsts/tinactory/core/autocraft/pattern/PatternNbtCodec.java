package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.logistics.IStackKey;
import org.shsts.tinactory.core.util.CodecHelper;

import java.util.ArrayList;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PatternNbtCodec {
    private final Codec<IMachineConstraint> constraintCodec;
    private final Codec<IStackKey> keyCodec;

    public PatternNbtCodec(Codec<IMachineConstraint> constraintCodec, Codec<IStackKey> keyCodec) {
        this.constraintCodec = constraintCodec;
        this.keyCodec = keyCodec;
    }

    public Codec<IStackKey> keyCodec() {
        return keyCodec;
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

    public CraftAmount decodeAmount(CompoundTag tag) {
        var key = CodecHelper.parseTag(keyCodec, tag.get("key"));
        return new CraftAmount(key, tag.getLong("amount"));
    }

    public CompoundTag encodeAmount(CraftAmount amount) {
        return encodeAmount(amount.key(), amount.amount());
    }

    public CompoundTag encodeAmount(IStackKey key, long amount) {
        var tag = new CompoundTag();
        tag.put("key", CodecHelper.encodeTag(keyCodec, key));
        tag.putLong("amount", amount);
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
            constraintsTag.add(CodecHelper.encodeTag(constraintCodec, constraint));
        }
        tag.put("constraints", constraintsTag);
        return tag;
    }

    private MachineRequirement decodeMachineRequirement(CompoundTag tag) {
        var constraintsTag = tag.getList("constraints", TAG_COMPOUND);
        var constraintsOut = new ArrayList<IMachineConstraint>(constraintsTag.size());
        for (var i = 0; i < constraintsTag.size(); i++) {
            constraintsOut.add(CodecHelper.parseTag(constraintCodec, constraintsTag.get(i)));
        }
        return new MachineRequirement(
            new ResourceLocation(tag.getString("recipeTypeId")),
            tag.getInt("voltageTier"),
            constraintsOut);
    }
}
