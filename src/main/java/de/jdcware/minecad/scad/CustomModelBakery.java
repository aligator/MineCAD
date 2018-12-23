package de.jdcware.minecad.scad;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class CustomModelBakery extends ModelBakery {

	public CustomModelBakery(IResourceManager resourceManagerIn, TextureMap textureMapIn, BlockModelShapes blockModelShapesIn) {
		super(resourceManagerIn, textureMapIn, blockModelShapesIn);

	}

	public static CustomModelBakery getInstance() {
		ModelManager modelManager = (ModelManager) ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "modelManager");
		CustomModelBakery bakery = new CustomModelBakery(Minecraft.getMinecraft().getResourceManager(), modelManager.getTextureMap(), modelManager.getBlockModelShapes());
		bakery.setupModelRegistry();
		return bakery;
	}

	public Map<ModelBlockDefinition, Collection<ModelResourceLocation>> getMultipartVariantMap() {
		return ObfuscationReflectionHelper.getPrivateValue(ModelBakery.class, this, "multipartVariantMap");
	}
}
