package de.jdcware.minecad.core;

import net.minecraft.client.renderer.block.model.ModelRotation;

public enum CadRotation {
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
    private final int getRotationY;

    CadRotation(int rotationX, int getRotationY) {
        this.rotationX = rotationX;
        this.getRotationY = getRotationY;
    }

    public static CadRotation getByModelRotation(ModelRotation modelRotation) {
        return CadRotation.valueOf(modelRotation.name());
    }

    public int getRotationX() {
        return rotationX;
    }

    public int getRotationY() {
        return getRotationY;
    }
}
