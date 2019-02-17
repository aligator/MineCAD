package de.jdcware.minecad.core;

import net.minecraft.client.renderer.block.model.ModelRotation;

abstract public class BaseCADModel implements ICADModel {

    protected final CadRotation rotation;

    public BaseCADModel() {
        this(null);
    }

    public BaseCADModel(ModelRotation rotation) {
        if (rotation == null) {
            rotation = ModelRotation.X0_Y0;
        }

        this.rotation = CadRotation.getByModelRotation(rotation);
    }

    @Override
    public CadRotation getRotation() {
        return rotation;
    }
}
