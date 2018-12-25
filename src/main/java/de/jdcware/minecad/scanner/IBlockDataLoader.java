package de.jdcware.minecad.scanner;

import net.minecraft.block.state.IBlockState;

public interface IBlockDataLoader {

	IBlockData loadBlockData(IBlockState blockState);
}
