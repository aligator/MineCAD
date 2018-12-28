package de.jdcware.minecad.scanner;

import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.VariantList;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

/**
 * A wraper around the original ModelBakery. It provides reflective access to some private fields.
 */
public class VanillaBakeryWrapper extends ModelBakery {

	private final Field variantsField;
	private final Field modelsField;
	private final Field multipartVariantMapField;

	public VanillaBakeryWrapper(IResourceManager resourceManagerIn, TextureMap textureMapIn, BlockModelShapes blockModelShapesIn) {
		super(resourceManagerIn, textureMapIn, blockModelShapesIn);

		this.variantsField = ObfuscationReflectionHelper.findField(ModelBakery.class, "field_177612_i"); // variants
		this.modelsField = ObfuscationReflectionHelper.findField(ModelBakery.class, "field_177611_h"); // models
		this.multipartVariantMapField = ObfuscationReflectionHelper.findField(ModelBakery.class, "field_188642_k"); // multipartVariantMap

	}

	@Override
	public void loadBlocks() {
		super.loadBlocks();
	}

	@Override
	public void loadVariantItemModels() {
		super.loadVariantItemModels();
	}

	/**
	 * Provides reflective access to the loadModelsCheck() function.
	 */
	public void loadModelsCheck() {
		try {
			ObfuscationReflectionHelper.findMethod(ModelBakery.class, "func_177597_h", void.class).invoke(this, null); // loadModelsCheck
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (
				InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Provides reflective access to the variants field.
	 */
	public Map<ModelResourceLocation, VariantList> getVariants() {
		try {
			return (Map<ModelResourceLocation, VariantList>) variantsField.get(this);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Provides reflective access to the modelsField field.
	 */
	public Map<ResourceLocation, ModelBlock> getModels() {
		try {
			return (Map<ResourceLocation, ModelBlock>) modelsField.get(this);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Provides reflective access to the multipartVariantMapField field.
	 */
	public Map<ModelBlockDefinition, Collection<ModelResourceLocation>> getMultipartVariantMap() {
		try {
			return (Map<ModelBlockDefinition, Collection<ModelResourceLocation>>) multipartVariantMapField.get(this);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
