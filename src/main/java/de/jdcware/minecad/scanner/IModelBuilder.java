package de.jdcware.minecad.scanner;

import de.jdcware.minecad.core.IBaseBlockData;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelRotation;

/**
 * A interface for the builder of block models.
 *
 * @param <T> The type of the resulting block model.
 */
public interface IModelBuilder<T> {

	void add(IBaseBlockData blockData);
	void add(ModelBlock modelData, ModelRotation modelRotation);

	T build();
}
