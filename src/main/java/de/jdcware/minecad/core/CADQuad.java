package de.jdcware.minecad.core;

import net.minecraft.client.renderer.block.model.BlockPartRotation;

import java.util.Collections;
import java.util.List;

public class CADQuad {

    private final List<CADPoint> points;
    private final BlockPartRotation rotation;

    public CADQuad(List<CADPoint> points, BlockPartRotation rotation) {
        this.points = Collections.unmodifiableList(points);
        this.rotation = rotation;
    }

    public List<CADPoint> getPoints() {
        return points;
    }

    public BlockPartRotation getRotation() {
        return rotation;
    }
}
