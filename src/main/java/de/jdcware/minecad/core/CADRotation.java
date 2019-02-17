package de.jdcware.minecad.core;

import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.vecmath.Quat4f;
import java.util.Optional;

public enum CADRotation {
    X0_Y0(0, 0),
    X0_Y90(0, 90),
    X0_Y180(0, 180),
    X0_Y270(0, 270),
    X90_Y0(90, 0),
    X90_Y90(90, 90),
    X90_Y180(90, 180),
    X90_Y270(90, 270),
    X180_Y0(180, 0),
    X180_Y90(180, 90),
    X180_Y180(180, 180),
    X180_Y270(180, 270),
    X270_Y0(270, 0),
    X270_Y90(270, 90),
    X270_Y180(270, 180),
    X270_Y270(270, 270);

    private final int rotationX;
    private final int rotationY;

    private final Quat4f leftRotation;
    private final Quat4f rightRotation;

    CADRotation(int rotationX, int getRotationY) {
        this.rotationX = rotationX;
        this.rotationY = getRotationY;

        ModelRotation modelRotation = ModelRotation.valueOf(this.name());
        Optional<TRSRTransformation> trOp = modelRotation.apply(Optional.empty());
        this.leftRotation = trOp.get().getLeftRot();
        this.rightRotation = trOp.get().getRightRot();
    }

    public static CADRotation getByModelRotation(ModelRotation modelRotation) {
        return CADRotation.valueOf(modelRotation.name());
    }

    /**
     * Get the model rotation by a model state.
     * <p>
     * As the it is not always easy to get the ModelRotation and there is also no simple way to get the rotation from a modelState,
     * this function calculates how the modelState rotates something and checks if any of the rotations in this enum match the rotations.
     * <p>
     * This has no good performance, but it should always work.
     *
     * @param modelState
     * @return
     */
    public static CADRotation getByModelState(IModelState modelState) {
        Optional<TRSRTransformation> trOp = modelState.apply(Optional.empty());
        Quat4f leftRotation = trOp.get().getLeftRot();
        Quat4f rightRotation = trOp.get().getRightRot();

        for (CADRotation rotation : CADRotation.values()) {
            if (rotation.leftRotation.equals(leftRotation) && rotation.rightRotation.equals(rightRotation)) {
                return rotation;
            }
        }

        return CADRotation.X0_Y0;
    }

    public int getRotationX() {
        return rotationX;
    }

    public int getRotationY() {
        return rotationY;
    }
}
