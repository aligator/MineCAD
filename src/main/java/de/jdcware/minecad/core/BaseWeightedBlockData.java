package de.jdcware.minecad.core;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.Variant;

import java.util.Map;

public class BaseWeightedBlockData implements IBaseBlockData {

	private final Map<Variant, BaseBlockData> models;


	public BaseWeightedBlockData(Map<Variant, BaseBlockData> models) {
		this.models = models;
	}

	@Override
	public ModelBlock getModelBlock() {
		// todo return evtl. random ???
		if (models.size() > 0) {
			return models.get(0).getModelBlock();
		} else {
			return null;
		}
	}

	@Override
	public ModelRotation getModelRotation() {
		// todo return evtl. random ???
		if (models.size() > 0) {
			return models.get(0).getModelRotation();
		} else {
			return null;
		}
	}
}
