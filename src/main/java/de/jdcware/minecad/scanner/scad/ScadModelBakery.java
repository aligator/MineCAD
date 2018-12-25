package de.jdcware.minecad.scanner.scad;

import com.google.common.base.Predicate;
import de.jdcware.minecad.MineCAD;
import de.jdcware.minecad.scanner.IBlockData;
import de.jdcware.minecad.scanner.VanillaBakeryWrapper;
import eu.printingin3d.javascad.models.Abstract3dModel;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantList;
import net.minecraft.client.renderer.block.model.multipart.Multipart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.registry.RegistrySimple;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ScadModelBakery {

	protected final IResourceManager resourceManager;
	protected final BlockModelShapes blockModelShapes;
	protected final RegistrySimple<ModelResourceLocation, IBlockData> baked3dRegistry = new RegistrySimple<>();

	private final float blockOverhang;
	private final float minSize;
	private final VanillaBakeryWrapper vanillaBakery;

	public ScadModelBakery(float blockOverhang, float minSize, IResourceManager resourceManagerIn, BlockModelShapes blockModelShapesIn) {
		this.vanillaBakery = new VanillaBakeryWrapper(resourceManagerIn, null, blockModelShapesIn);

		this.blockOverhang = blockOverhang;
		this.minSize = minSize;
		this.resourceManager = resourceManagerIn;
		this.blockModelShapes = blockModelShapesIn;
	}

	public static ScadModelBakery getInstance(float blockOverhang, float minSize) {
		return new ScadModelBakery(blockOverhang, minSize, Minecraft.getMinecraft().getResourceManager(), Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes());
	}

	public IRegistry<ModelResourceLocation, IBlockData> setup3dModelRegistry() {
		vanillaBakery.loadBlocks();
		vanillaBakery.loadVariantItemModels();
		vanillaBakery.loadModelsCheck();

		this.bakeBlockModels();
		return this.baked3dRegistry;
	}

	private void bakeBlockModels() {
		for (ModelResourceLocation modelresourcelocation : vanillaBakery.getVariants().keySet()) {
			IBlockData bakedBlockData = this.createRandomModelForVariantList(vanillaBakery.getVariants().get(modelresourcelocation), modelresourcelocation.toString());

			if (bakedBlockData != null) {
				this.baked3dRegistry.putObject(modelresourcelocation, bakedBlockData);
			}
		}

		for (Map.Entry<ModelBlockDefinition, Collection<ModelResourceLocation>> entry : vanillaBakery.getMultipartVariantMap().entrySet()) {
			ModelBlockDefinition modelblockdefinition = entry.getKey();
			Multipart multipart = modelblockdefinition.getMultipartData();
			String s = (Block.REGISTRY.getNameForObject(multipart.getStateContainer().getBlock())).toString();
			Map<Predicate<IBlockState>, Abstract3dModel> selectors = new HashMap<>();

			for (Selector selector : multipart.getSelectors()) {
				IBlockData<Abstract3dModel> bakedSelectorData = this.createRandomModelForVariantList(selector.getVariantList(), "selector of " + s);

				if (bakedSelectorData != null) {
					selectors.put(selector.getPredicate(multipart.getStateContainer()), bakedSelectorData.getBlockParts(null));
				}
			}

			IBlockData<Abstract3dModel> bakedMultiBlockData = new ScadMultiBlockData(selectors);

			for (ModelResourceLocation modelresourcelocation1 : entry.getValue()) {
				if (!modelblockdefinition.hasVariant(modelresourcelocation1.getVariant())) {
					this.baked3dRegistry.putObject(modelresourcelocation1, bakedMultiBlockData);
				}
			}
		}
	}

	@Nullable
	private IBlockData<Abstract3dModel> createRandomModelForVariantList(VariantList variantsIn, String modelLocation) {
		if (variantsIn.getVariantList().isEmpty()) {
			return null;
		} else {
			ScadBlockBuilder builder = new ScadBlockBuilder(blockOverhang, minSize);

			Variant variant = variantsIn.getVariantList().get(0);
			ModelBlock modelblock = vanillaBakery.getModels().get(variant.getModelLocation());

			if (modelblock != null && modelblock.isResolved()) {
				if (modelblock.getElements().isEmpty()) {
					MineCAD.LOGGER.warn("Missing elements for: {}", modelLocation);
				} else {
					builder.add(modelblock, variant.getRotation());
				}
			} else {
				MineCAD.LOGGER.warn("Missing model for: {}", (Object) modelLocation);
			}

			return new ScadBlockData(builder.build());
// TODO: support different variants

/*			int i = 0;

			for (Variant variant : variantsIn.getVariantList())
			{
				ModelBlock modelblock = getModels().get(variant.getModelLocation());

				if (modelblock != null && modelblock.isResolved())
				{
					if (modelblock.getElements().isEmpty())
					{
						MineCAD.LOGGER.warn("Missing elements for: {}", (Object)modelLocation);
					}
					else
					{
						IBlockData blockData = this.bakeModel(modelblock, variant.getRotation(), variant.isUvLock());

						if (blockData != null)
						{
							++i;
							builder.add(blockData);
						}
					}
				}
				else
				{
					MineCAD.LOGGER.warn("Missing model for: {}", (Object)modelLocation);
				}
			}

			IBlockData blockData1 = null;

			if (i == 0)
			{
				MineCAD.LOGGER.warn("No weighted models for: {}", (Object)modelLocation);
			}
			else if (i == 1)
			{
				blockData1 = weightedbakedmodel$builder.first();
			}
			else
			{
				blockData1 = weightedbakedmodel$builder.build();
			}

			return blockData1;*/
		}
	}
}