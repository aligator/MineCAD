package de.jdcware.minecad.core;

abstract public class BaseCADModel implements ICADModel {

    private final CADRotation rotation;

    public BaseCADModel() {
        this(CADRotation.X0_Y0);
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
}
