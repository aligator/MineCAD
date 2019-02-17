package de.jdcware.minecad.core;

abstract public class BaseCADBuilder<T> implements ICADBuilder<T> {

    private final Class<T> clazz;

    protected BaseCADBuilder(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<T> getDestClass() {
        return clazz;
    }
}
