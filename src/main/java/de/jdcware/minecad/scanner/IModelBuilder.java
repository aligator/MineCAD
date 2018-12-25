package de.jdcware.minecad.scanner;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelRotation;

public interface IModelBuilder<T> {

	void add(ModelBlock modelData, ModelRotation modelRotation);

	T build();
}
