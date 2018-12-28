package de.jdcware.minecad.scanner;

import net.minecraft.block.state.IBlockState;

/**
 * A interface for the block data. It may be dependend on the block state.
 *
 * @param <T> The type the block parts are.
 */
public interface IBlockData<T> {

	T getBlockModelData(IBlockState state);
}
