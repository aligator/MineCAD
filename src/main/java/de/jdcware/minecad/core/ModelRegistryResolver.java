package de.jdcware.minecad.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.client.model.IModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelRegistryResolver {

	private IModel originModel;
	private List<ModelRotation> modelRotations;
	private Map<IModel, IBaseBlockData> models = Maps.newHashMap();


	public ModelRegistryResolver() {
		originModel = null;
	}

	public void resolveMissing() {
		if (this.originModel != null) {
			// Todo: add default:
			// addToRegistry(this.originModel, );
		}
	}

	public void addWithDependency(IModel model, ModelRotation rotation) {
		if (this.originModel == null) {
			this.originModel = model;
			this.modelRotations = Lists.newArrayList();
			modelRotations.add(rotation);
		}
	}

	public void addWithDependency(IModel model, List<Variant> variants) {
		// I rely on the fact that each variant is added (baked) directly after this one and that each object with dependency has no dependency which itself has dependencies. But I'm not sure about this...
		if (this.originModel == null) {
			this.originModel = model;
			this.modelRotations = variants.stream().map(variant -> variant.getRotation()).collect(Collectors.toList());
		}
	}

	public void add(IModel model, IBaseBlockData blockData) {
		if (this.originModel != null) {
			if (modelRotations.size() > 0) {
				addToRegistry(this.originModel, new BaseBlockData(blockData.getModelBlock(), modelRotations.get(0)));
				modelRotations.remove(0);
			}

			if (modelRotations.size() == 0) {
				this.originModel = null;
			}
		} else {
			addToRegistry(model, blockData);
			this.originModel = null;
		}
	}

	private void addToRegistry(IModel model, IBaseBlockData blockData) {
		if (model != null && blockData != null) {
			models.put(model, blockData);
		}
	}

	public RegistrySimple<ModelResourceLocation, IBaseBlockData> getModelRegistry(Map<ModelResourceLocation, IModel> stateModels) {
		RegistrySimple<ModelResourceLocation, IBaseBlockData> registry = new RegistrySimple<>();

		for (Map.Entry<ModelResourceLocation, IModel> e : stateModels.entrySet()) {
			if (models.containsKey(e.getValue())) {
				registry.putObject(e.getKey(), models.get(e.getValue()));
			}
		}
		return registry;
	}
}
