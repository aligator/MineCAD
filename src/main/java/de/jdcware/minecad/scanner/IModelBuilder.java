package de.jdcware.minecad.scanner;


import de.jdcware.minecad.core.ICADModel;
import net.minecraft.block.state.IBlockState;

/**
 * A interface for the builder of block models.
 *
 * @param <T> The type of the resulting block model.
 */
public interface IModelBuilder<T> {

    void add(ICADModel cadModel, IBlockState state);

	T build();
}
