package de.jdcware.minecad.core;

import net.minecraft.client.renderer.block.model.BlockPartRotation;
import org.lwjgl.util.vector.Vector3f;

import java.util.List;

public interface ICADBuilder<T> {

    Class<T> getDestClass();

    T quad(Vector3f from, Vector3f to, BlockPartRotation rotation);

    T union(List<T> models);

    T rotate(T model, CADRotation blockRotation);
}
