package de.jdcware.minecad.scanner;

import net.minecraft.block.state.IBlockState;

import java.util.List;

public interface IBlockDataLoader {

	List<IBlockData> loadBlockData(IBlockState blockState);
}
