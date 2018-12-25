package de.jdcware.minecad.scanner;

import net.minecraft.block.state.IBlockState;

public interface IBlockData<T> {

	T getBlockParts(IBlockState state);
}
