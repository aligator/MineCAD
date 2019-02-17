package de.jdcware.minecad.core;

import net.minecraft.client.renderer.block.model.ModelRotation;

abstract public class BaseCADModel implements ICADModel {

    private CADRotation rotation;

    public BaseCADModel() {
        this(CADRotation.X0_Y0);
    }

    public BaseCADModel(ModelRotation rotation) {
        if (rotation == null) {
            rotation = ModelRotation.X0_Y0;
        }

        this.rotation = CADRotation.getByModelRotation(rotation);
    }

    public BaseCADModel(CADRotation rotation) {
        if (rotation == null) {
            rotation = CADRotation.X0_Y0;
        }

        this.rotation = rotation;
    }

    @Override
    public CADRotation getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(ModelRotation rotation) {
        this.rotation = CADRotation.getByModelRotation(rotation);
    }
}
