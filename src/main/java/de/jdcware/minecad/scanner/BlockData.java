package de.jdcware.minecad.scanner;

import net.minecraft.client.renderer.block.model.BlockPart;

import java.util.List;

public class BlockData implements IBlockData {

	private final int xRotation;
	private final int yRotation;
	private final List<BlockPart> blockParts;

	BlockData(int xRotation, int yRotation, List<BlockPart> blockParts) {
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