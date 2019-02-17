package de.jdcware.minecad.core;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelRotation;

import javax.annotation.Nullable;

public interface ICADModel {

    Object buildModel(ICADBuilder builder, @Nullable IBlockState state, long rand);

    CADRotation getRotation();

    void setRotation(ModelRotation rotation);
}
