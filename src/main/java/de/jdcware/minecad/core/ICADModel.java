package de.jdcware.minecad.core;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nullable;

public interface ICADModel {

    Object buildModel(ICADBuilder builder, @Nullable IBlockState state, long rand);

    CADRotation getRotation();
}
