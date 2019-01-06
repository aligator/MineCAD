package de.jdcware.minecad.core;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelRotation;

public class BaseBlockData implements IBaseBlockData {

	private final ModelBlock model;
	private final ModelRotation rotation;

	public BaseBlockData(ModelBlock model) {
		this.model = model;
		this.rotation = ModelRotation.X0_Y0;
	}

	public BaseBlockData(ModelBlock model, ModelRotation rotation) {
		this.model = model;
		this.rotation = rotation;
	}

	@Override
	public ModelBlock getModelBlock() {
		return model;
	}

	@Override
	public ModelRotation getModelRotation() {
		return rotation;
	}
}
