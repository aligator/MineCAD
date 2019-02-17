package de.jdcware.minecad.core;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import java.util.List;

public interface ICADModel {

    // TODO: Facing really needed???
    List<CADQuad> getQuads(IBlockState state, EnumFacing facing, long rand);

    CadRotation getRotation();
}
