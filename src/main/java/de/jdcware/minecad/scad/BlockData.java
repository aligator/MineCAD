package de.jdcware.minecad.scad;

import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraftforge.common.model.IModelPart;

import java.util.List;

public class BlockData implements IBlockData, IModelPart {

	private final int xRotation;
	private final int yRotation;
	private final List<BlockPart> blockParts;

	public BlockData(int xRotation, int yRotation, List<BlockPart> blockParts) {
		this.xRotation = xRotation;
		this.yRotation = yRotation;
		this.blockParts = blockParts;
	}

	@Override
	public int getXRotation() {
		return xRotation;
	}

	@Override
	public int getYRotation() {
		return yRotation;
	}

	@Override
	public List<BlockPart> getBlockParts() {
		return blockParts;
	}
}
