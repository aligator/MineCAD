package de.jdcware.minecad.core;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelRotation;

public interface IBaseBlockData {

	ModelBlock getModelBlock();

	ModelRotation getModelRotation();
}
