package de.jdcware.minecad.scad;

import de.jdcware.minecad.MineCAD;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

@SideOnly(Side.CLIENT)
public class ResourceParser {
	/**
	 * A {@link StateMapperBase} used to create property strings.
	 */
	private final static StateMapperBase propertyStringMapper = new StateMapperBase() {
		@Override
		protected ModelResourceLocation getModelResourceLocation(final IBlockState state) {
			return new ModelResourceLocation("minecraft:air");
		}
	};

	public static BlockData parse(IBlockState blockState) throws IOException {
		ResourceLocation location = blockState.getBlock().getRegistryName();

		//ModelBlock block = manager.getModel(location);

		// get variant
		String variant = propertyStringMapper.getPropertyString(blockState.getProperties());


		// get resource blockstate stream
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream in = classloader.getResourceAsStream("assets/" + location.getResourceDomain() + "/blockstates/" + location.getResourcePath() + ".json");

		// if stream is null, the blockstates-asset didn't exist
		if (in != null) {
			ModelBlockDefinition def = ModelBlockDefinition.parseFromReader(new InputStreamReader(in, StandardCharsets.UTF_8), location);


			// Todo: check Multipart??
			if (def.hasMultipartData()) {
			/*	if (MineCAD.mb == null) {
					MineCAD.LOGGER.info("mb is null");

				} else {
					MineCAD.LOGGER.info(MineCAD.mb.toString());
					MineCAD.LOGGER.info(MineCAD.mb.getMultipartVariantMap().toString());
					MineCAD.LOGGER.info(MineCAD.mb.getMultipartVariantMap().size());

					List<BakedQuad> list = Lists.<BakedQuad>newArrayList();
				}
					//Map<Predicate<IBlockState>, IBakedModel> variantMap = MineCAD.mb.getMultipartVariantMap();
/**
					for (Selector selector : selectors) {


						for (Map.Entry<Predicate<IBlockState>, IBakedModel> entry : MineCAD.mb.getMultipartVariantMap().entrySet())
						{
							if (((Predicate)entry.getKey()).apply(state))
							{
								list.addAll((entry.getValue()).getQuads(state, side, rand++));
							}
						}
					}
				}
*/
			}


			variant = getVariant(def, variant);


			if (variant == null && def.hasVariant("normal")) {
				variant = "normal";
			}


			if (variant != null) {
				MineCAD.LOGGER.info("loading variant: " + variant);
				ModelRotation rotation = (ModelRotation) def.getVariant(variant).getVariantList().get(0).getState();
				return new BlockData(getModelRotationX(rotation) * 90, getModelRotationY(rotation) * 90, parseBlockData(def.getVariant(variant).getVariantList().get(0).getModelLocation()).getElements());
			}

		} else {
			// if no blockstats exist, try block directly (should normally not be needed...
			// other way to check if resource exists???
			in = classloader.getResourceAsStream("assets/models" + location.getResourceDomain() + "/block/" + location.getResourcePath() + ".json");

			if (in != null) {
				return new BlockData(0, 0, parseBlockData(location).getElements());
			}
		}

		return new BlockData(0, 0, parseBlockData(new ResourceLocation("minecraft", "cube")).getElements());
	}

	/**
	 * recursively parse the block data until a model is found
	 *
	 * @param location
	 * @return
	 * @throws IOException
	 */
	private static ModelBlock parseBlockData(ResourceLocation location) throws IOException {
		String resourcePath = location.getResourcePath();
		if (!resourcePath.contains("block/")) {
			resourcePath = "block/" + resourcePath;
		}

		String path = "assets/" + location.getResourceDomain() + "/models/" + resourcePath + ".json";
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		if (classloader.getResource(path) != null) {
			InputStream in = classloader.getResourceAsStream(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			ModelBlock block = ModelBlock.deserialize(reader);
			in.close();
			reader.close();

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

	/**
	 * tries variant, if it doesn't work, it removes the last property and tries it again. And so on...
	 * Needed, because for example doors don't have "powered" property in the variants (WHY???)
	 *
	 * @param definition model block definition in which the variant should be searched
	 * @param variant    tested variant
	 * @return the working variant or null if none was found
	 */
	private static String getVariant(ModelBlockDefinition definition, String variant) {
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

	private static int getModelRotationX(ModelRotation state) {
		return (int) getReflective(ModelRotation.class, state, "quartersX", 0);
	}

	private static int getModelRotationY(ModelRotation state) {
		return (int) getReflective(ModelRotation.class, state, "quartersY", 0);
	}

	private static ModelManager getModelManager() {
		ModelManager modelManager = (ModelManager) getReflective(Minecraft.class, Minecraft.getMinecraft(), "modelManager", null);
		return modelManager;

		//BlockModelShapes modelProvider = (BlockModelShapes) getReflective(ModelManager.class, modelManager, "modelProvider", null);
	}

	private static Object getReflective(Class instanceClass, Object instance, String field, Object defaultValue) {
		try {
			Field classfield = ObfuscationReflectionHelper.findField(instanceClass, field);
			classfield.setAccessible(true);
			return classfield.get(instance);
		} catch (IllegalAccessException e) {
			MineCAD.LOGGER.info("Reflective access failed");
			e.printStackTrace();
		}
		return defaultValue;
	}
}

