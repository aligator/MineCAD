package de.jdcware.minecad.core;

import net.minecraft.block.state.IBlockState;

abstract public class BaseCADBuilder<T> implements ICADBuilder<T> {

    private final Class<T> clazz;

    protected BaseCADBuilder(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<T> getDestClass() {
        return clazz;
    }

    @Override
    public T buildModel(ICADModel model, IBlockState blockState, long rand) {

        return getDestClass().cast(model.buildModel(this, blockState, rand));
    }
}
