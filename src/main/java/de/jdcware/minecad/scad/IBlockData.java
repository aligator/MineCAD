package de.jdcware.minecad.scad;

import net.minecraft.client.renderer.block.model.BlockPart;

import java.util.List;

public interface IBlockData {

	int getXRotation();

	int getYRotation();

	List<BlockPart> getBlockParts();
}
