package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Codec;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.pattern.MachineConstraintHelper;

public record TestMachineConstraint(String value) implements IMachineConstraint {
    public static final String TYPE_ID = "test:constraint";
    public static final Codec<TestMachineConstraint> CODEC =
        Codec.STRING.xmap(TestMachineConstraint::new, TestMachineConstraint::value);

    public static final Codec<IMachineConstraint> MACHINE_CONSTRAINT_CODEC = Codec.STRING.dispatch(
        IMachineConstraint::typeId,
        typeId -> typeId.equals(TYPE_ID) ? CODEC : MachineConstraintHelper.codec(typeId)
    );

    @Override
    public String typeId() {
        return TYPE_ID;
    }
}
