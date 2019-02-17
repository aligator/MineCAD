package de.jdcware.minecad.core;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * based on {@link net.minecraft.client.renderer.block.model.MultipartBakedModel}
 */
public class MultipartCADModel extends BaseCADModel {

    private final Map<Predicate<IBlockState>, ICADModel> selectors;

    public MultipartCADModel(Map<Predicate<IBlockState>, ICADModel> selectors) {
        super();

        this.selectors = selectors;
    }

    @Override
    public Object buildModel(ICADBuilder builder, IBlockState state, long rand) {
        List<Object> allModels = Lists.newArrayList();
        if (state != null) {
            Iterator iterSelectors = this.selectors.entrySet().iterator();

            while (iterSelectors.hasNext()) {
                Map.Entry<Predicate<IBlockState>, ICADModel> selectorParts = (Map.Entry) iterSelectors.next();
                if ((selectorParts.getKey()).apply(state)) {

                    allModels.add((selectorParts.getValue()).buildModel(builder, state, rand++));
                }
            }
        }

        return builder.union(allModels);
    }

    public static class Builder {
        private final Map<Predicate<IBlockState>, ICADModel> builderSelectors = Maps.newLinkedHashMap();

        public Builder() {
        }

        public void putModel(Predicate<IBlockState> predicate, ICADModel model) {
            this.builderSelectors.put(predicate, model);
        }

        public ICADModel makeMultipartModel() {
            return new MultipartCADModel(this.builderSelectors);
        }
    }
}
