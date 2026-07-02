package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
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

    public Codec<CraftPattern> patternCodec() {
        return CompoundTag.CODEC.xmap(this::decodePattern, this::encodePattern);
    }

    public CompoundTag encodePattern(CraftPattern pattern) {
        var tag = new CompoundTag();
        tag.putUUID("patternUuid", pattern.patternUuid());
        tag.put("inputs", encodeAmounts(pattern.inputs()));
        tag.put("outputs", encodeAmounts(pattern.outputs()));
        tag.put("constraints", encodeConstraints(pattern.constraints()));
        return tag;
    }

    public CraftPattern decodePattern(CompoundTag tag) {
        if (!tag.hasUUID("patternUuid")) {
            throw new IllegalArgumentException("patternUuid is required");
        }
        return new CraftPattern(
            tag.getUUID("patternUuid"),
            decodeAmounts(tag.getList("inputs", TAG_COMPOUND)),
            decodeAmounts(tag.getList("outputs", TAG_COMPOUND)),
            decodeConstraints(tag.getList("constraints", TAG_COMPOUND)));
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

    private ListTag encodeConstraints(Iterable<IMachineConstraint> constraints) {
        var constraintsTag = new ListTag();
        for (var constraint : constraints) {
            constraintsTag.add(CodecHelper.encodeTag(constraintCodec, constraint));
        }
        return constraintsTag;
    }

    private ArrayList<IMachineConstraint> decodeConstraints(ListTag list) {
        var ret = new ArrayList<IMachineConstraint>(list.size());
        for (var i = 0; i < list.size(); i++) {
            ret.add(CodecHelper.parseTag(constraintCodec, list.get(i)));
        }
        return ret;
    }
}
