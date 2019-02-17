package de.jdcware.minecad.core;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * based on {@link net.minecraft.client.renderer.block.model.WeightedBakedModel}
 */
public class WeightedRandomCADModel extends BaseCADModel {

    private final int totalWeight;
    private final List<WeightedModel> models;
    private final ICADModel baseModel;

    public WeightedRandomCADModel(List<WeightedModel> models) {
        super();

        this.models = models;
        this.totalWeight = WeightedRandom.getTotalWeight(models);
        this.baseModel = (models.get(0)).model;
    }

    private ICADModel getRandomModel(long rand) {
        return (WeightedRandom.getRandomItem(this.models, Math.abs((int) rand >> 16) % this.totalWeight)).model;
    }

    public List<CADQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing facing, long rand) {
        return this.getRandomModel(rand).getQuads(state, facing, rand);
    }

    @SideOnly(Side.CLIENT)
    static class WeightedModel extends WeightedRandom.Item implements Comparable<WeightedModel> {
        protected final ICADModel model;

        public WeightedModel(ICADModel model, int weight) {
            super(weight);
            this.model = model;
        }

        public int compareTo(WeightedModel comparedObject) {
            return ComparisonChain.start().compare(comparedObject.itemWeight, this.itemWeight).result();
        }

        public String toString() {
            return "MyWeighedRandomItem{weight=" + this.itemWeight + ", model=" + this.model + '}';
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Builder {
        private final List<WeightedModel> listItems = Lists.newArrayList();

        public Builder() {
        }

        public Builder add(ICADModel model, int weight) {
            this.listItems.add(new WeightedModel(model, weight));
            return this;
        }

        public WeightedRandomCADModel build() {
            Collections.sort(this.listItems);
            return new WeightedRandomCADModel(this.listItems);
        }

        public ICADModel first() {
            return (this.listItems.get(0)).model;
        }
    }
}
