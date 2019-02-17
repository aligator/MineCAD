package de.jdcware.minecad.core;

import net.minecraft.client.renderer.block.model.BlockPartRotation;
import org.lwjgl.util.vector.Vector3f;

public class CADQuad {

    private final Vector3f from;
    private final Vector3f to;
    private final BlockPartRotation rotation;

    public CADQuad(Vector3f from, Vector3f to, BlockPartRotation rotation) {
        this.from = from;
        this.to = to;
        this.rotation = rotation;
    }

    public Vector3f getFrom() {
        return from;
    }

    public Vector3f getTo() {
        return to;
    }

    public BlockPartRotation getRotation() {
        return rotation;
    }
}
