package de.jdcware.minecad.scanner;

import de.jdcware.minecad.MineCAD;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.multipart.Multipart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ResourceParser implements IBlockDataLoader {
	/**
	 * A {@link StateMapperBase} used to create property strings.
	 */
	private final static StateMapperBase propertyStringMapper = new StateMapperBase() {
		@Override
		protected ModelResourceLocation getModelResourceLocation(final IBlockState state) {
			return new ModelResourceLocation("minecraft:air");
		}
	};

	/**
	 * recursively parse the block data until a model is found
	 *
	 * @param location
	 * @return
	 * @throws IOException
	 */
	private static ModelBlock parseBlockData(ResourceLocation location) {
		String resourcePath = location.getResourcePath();
		if (!resourcePath.contains("block/")) {
			resourcePath = "block/" + resourcePath;
		}

		String path = "assets/" + location.getResourceDomain() + "/models/" + resourcePath + ".json";
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		if (classloader.getResource(path) != null) {
			ModelBlock block = null;
			try {
				InputStream in = classloader.getResourceAsStream(path);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));

				block = ModelBlock.deserialize(reader);
				in.close();
				reader.close();
			} catch (IOException ex) {
				MineCAD.LOGGER.error("Couldn't load block model " + location.toString());
				ex.printStackTrace();
			}

			if (block.getElements().size() > 0) {
				return block;
			} else if (block.getParentLocation().getResourceDomain().equals(location.getResourceDomain()) && block.getParentLocation().getResourcePath().equals(resourcePath)) {
				return null;
			} else {
				return parseBlockData(block.getParentLocation());
			}
		}
		return null;
	}

	private static Object getReflective(Class instanceClass, Object instance, String field) {
		String internalClassName = FMLDeobfuscatingRemapper.INSTANCE.unmap(Type.getInternalName(instanceClass));
		return ObfuscationReflectionHelper.getPrivateValue(instanceClass, instance, field);
	}

	@Override
	public List<IBlockData> loadBlockData(IBlockState blockState) {
		List<IBlockData> resultingBlocks = new ArrayList();

		CustomModelBakery modelBakery = MineCAD.modelBakery;

		//debugBlockModel(blockState);

		ResourceLocation location = modelBakery.getResourceLocation(blockState.getBlock().getRegistryName());

		String variant = propertyStringMapper.getPropertyString(blockState.getProperties());


		// get resource blockstate stream
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream in = classloader.getResourceAsStream("assets/" + location.getResourceDomain() + "/blockstates/" + location.getResourcePath() + ".json");

		if (in != null) {
			ModelBlockDefinition def = ModelBlockDefinition.parseFromReader(new InputStreamReader(in, StandardCharsets.UTF_8), location);

			if (def.hasMultipartData()) {
				Multipart currentMultiparts = def.getMultipartData();

				BlockStateContainer.Builder blockStateBuilder = new BlockStateContainer.Builder(blockState.getBlock());

				for (IProperty prop : blockState.getProperties().keySet()) {
					blockStateBuilder.add(prop);
				}
				currentMultiparts.setStateContainer(blockStateBuilder.build());


				//Variant lastFoundVariant = null;
				for (Selector selector : currentMultiparts.getSelectors()) {
					if (selector.getPredicate(currentMultiparts.getStateContainer()).apply(blockState)) {
						Variant foundVariant = selector.getVariantList().getVariantList().get(0);
						ModelRotation rotation = (ModelRotation) foundVariant.getState();
						resultingBlocks.add(new BlockData(getModelRotationX(rotation) * 90, getModelRotationY(rotation) * 90, parseBlockData(foundVariant.getModelLocation()).getElements()));

					}
				}
			} else {
				variant = getVariant(def, variant);


				if (variant == null && def.hasVariant("normal")) {
					variant = "normal";
				}


				if (variant != null) {
					ModelRotation rotation = (ModelRotation) def.getVariant(variant).getVariantList().get(0).getState();
					resultingBlocks.add(new BlockData(getModelRotationX(rotation) * 90, getModelRotationY(rotation) * 90, parseBlockData(def.getVariant(variant).getVariantList().get(0).getModelLocation()).getElements()));
				}
			}

		} else {
			// if no blockstats exist, try block directly (should normally not be needed...
			// other way to check if resource exists???
			in = classloader.getResourceAsStream("assets/models" + location.getResourceDomain() + "/block/" + location.getResourcePath() + ".json");

			if (in != null) {
				resultingBlocks.add(new BlockData(0, 0, parseBlockData(location).getElements()));
			}
		}

		if (resultingBlocks.size() == 0) {
			resultingBlocks.add(new BlockData(0, 0, parseBlockData(new ResourceLocation("minecraft", "cube")).getElements()));
		}

		return resultingBlocks;
	}

	/**
	 * tries variant, if it doesn't work, it removes the last property and tries it again. And so on...
	 * Needed, because for example doors don't have "powered" property in the variants (WHY???)
	 *
	 * @param definition model block definition in which the variant should be searched
	 * @param variant    tested variant
	 * @return the working variant or null if none was found
	 */
	private String getVariant(ModelBlockDefinition definition, String variant) {
		while (variant.length() > 0) {
			if (definition.hasVariant(variant)) {
				return variant;
			}

			String variants[] = variant.split(",");
			StringBuilder variantBuilder = new StringBuilder();
			for (int i = 0; i < variants.length - 1; i++) {
				if (variantBuilder.length() > 0) {
					variantBuilder.append(',');
				}
				variantBuilder.append(variants[i]);
			}
			variant = variantBuilder.toString();
		}

		return null;
	}

	private int getModelRotationX(ModelRotation state) {
		return (int) getReflective(ModelRotation.class, state, "field_177543_t"); // quartersX
	}

	private int getModelRotationY(ModelRotation state) {
		return (int) getReflective(ModelRotation.class, state, "field_177542_u"); // quartersY
	}

	private ModelManager getModelManager() {
		ModelManager modelManager = (ModelManager) getReflective(Minecraft.class, Minecraft.getMinecraft(), "modelManager");
		return modelManager;

		//BlockModelShapes modelProvider = (BlockModelShapes) getReflective(ModelManager.class, modelManager, "modelProvider", null);
	}

	private void debugBlockModel(IBlockState blockState) {
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(blockState);
		EnumFacing facing = EnumFacing.EAST;

		for (Comparable prop : blockState.getProperties().values()) {
			if (prop instanceof EnumFacing) {
				facing = (EnumFacing) prop;
			}
		}

		MineCAD.LOGGER.info(facing);


		for (EnumFacing face : EnumFacing.values()) {
			MineCAD.LOGGER.info(face);

			List<BakedQuad> quads = model.getQuads(blockState, face, 1);

			MineCAD.LOGGER.info(quads.size());

			int currentQuads = 0;
			for (BakedQuad quad : quads) {

				MineCAD.LOGGER.info("P0" + currentQuads + " ( "
						+ Float.intBitsToFloat(quad.getVertexData()[0]) + ", "
						+ Float.intBitsToFloat(quad.getVertexData()[1]) + ", "
						+ Float.intBitsToFloat(quad.getVertexData()[2]) + " ) ");

				MineCAD.LOGGER.info("P1" + currentQuads + " ( "
						+ Float.intBitsToFloat(quad.getVertexData()[7]) + ", "
						+ Float.intBitsToFloat(quad.getVertexData()[8]) + ", "
						+ Float.intBitsToFloat(quad.getVertexData()[9]) + " ) ");

				MineCAD.LOGGER.info("P2" + currentQuads + " ( "
						+ Float.intBitsToFloat(quad.getVertexData()[14]) + ", "
						+ Float.intBitsToFloat(quad.getVertexData()[15]) + ", "
						+ Float.intBitsToFloat(quad.getVertexData()[16]) + " ) ");

				MineCAD.LOGGER.info("P3" + currentQuads + " ( "
						+ Float.intBitsToFloat(quad.getVertexData()[21]) + ", "
						+ Float.intBitsToFloat(quad.getVertexData()[22]) + ", "
						+ Float.intBitsToFloat(quad.getVertexData()[23]) + " ) ");
				MineCAD.LOGGER.info("");

				currentQuads++;
			}

			MineCAD.LOGGER.info("--------------");
		}
	}
}

