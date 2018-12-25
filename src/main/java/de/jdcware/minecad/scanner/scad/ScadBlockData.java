package de.jdcware.minecad.scanner.scad;

import de.jdcware.minecad.scanner.IBlockData;
import eu.printingin3d.javascad.models.Abstract3dModel;
import net.minecraft.block.state.IBlockState;

public class ScadBlockData implements IBlockData<Abstract3dModel> {

	private final Abstract3dModel model;

	ScadBlockData(Abstract3dModel model) {
		this.model = model;
	}

	@Override
	public Abstract3dModel getBlockParts(IBlockState state) {
		return model;
	}
}
