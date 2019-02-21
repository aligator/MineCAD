package de.jdcware.minecad.core.asm;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import de.jdcware.minecad.MineCAD;
import de.jdcware.minecad.core.*;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.multipart.Multipart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.MultiModelState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

@IFMLLoadingPlugin.TransformerExclusions({"de.jdcware.minecad.core.asm"})
@IFMLLoadingPlugin.MCVersion(value = "1.12.2")
public class MineCADCorePlugin implements IFMLLoadingPlugin {

	public static final Logger LOGGER = LogManager.getLogger(MineCAD.MODID);

	public static Map<ModelResourceLocation, IModel> stateModels = Maps.newHashMap();

	// Models loaded by the hooks
	public static Map<IModel, ICADModel> loadedModels = Maps.newHashMap();

	// finished registry
	public static RegistrySimple<ModelResourceLocation, ICADModel> modelRegistry;

	// not needed for this mod
	public static Map<ModelResourceLocation, ModelBlockDefinition> multipartDefinitions = Maps.newHashMap();
	public static Map<ModelBlockDefinition, IModel> multipartModels = Maps.newHashMap();

	private static Method getModelField;

	/**
	 * Default implementation for IModel.getModel()
	 *
	 * @param model
	 * @return
	 */
	public static Optional<ICADModel> defaultGetModel(IModel model, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		return Optional.of(new SimpleCADModel(model, CADRotation.getByModelState(state)));
	}

	/**
	 * Loads some maps after the vanilla IModels are loaded. For this mod only the stateModels are needed.
	 *
	 * @param stateModels
	 * @param multipartDefinitions
	 * @param multipartModels
	 */
	public static void onBlocksLoaded(Map<ModelResourceLocation, IModel> stateModels, Map<ModelResourceLocation, ModelBlockDefinition> multipartDefinitions, Map<ModelBlockDefinition, IModel> multipartModels) {
        MineCADCorePlugin.stateModels = stateModels;

		// The other params are not needed for this mod, but maybe interesting for others...
        MineCADCorePlugin.multipartDefinitions = multipartDefinitions;
        MineCADCorePlugin.multipartModels = multipartModels;
	}

	public static void onBlockBake(IModel model, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		Optional<ICADModel> cadModel = callGetModel(model, state, format, bakedTextureGetter);
		if (cadModel.isPresent()) {
			if (cadModel.isPresent()) {
				loadedModels.put(model, cadModel.get());
			} else {
				// TODO: add default block
			}
		}
	}

	public static Optional<ICADModel> onWeightedRandomBlock(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, List<Variant> variants, List<ResourceLocation> locations, Set<ResourceLocation> textures, List<IModel> models, IModelState defaultState) {
		if (!Attributes.moreSpecific(format, Attributes.DEFAULT_BAKED_FORMAT)) {
			throw new IllegalArgumentException("can't bake vanilla weighted models to the format that doesn't fit into the default one: " + format);
		} else if (variants.size() == 1) {
			Optional<ICADModel> model = callGetModel(models.get(0), MultiModelState.getPartState(state, models.get(0), 0), format, bakedTextureGetter);
			if (model.isPresent()) {
				//model.get().setRotation(variants.get(0).getRotation());
				return model;
			}
			return Optional.empty();
		} else {
			WeightedRandomCADModel.Builder builder = new WeightedRandomCADModel.Builder();

			for (int i = 0; i < variants.size(); ++i) {
				Optional<ICADModel> model = callGetModel(models.get(i), MultiModelState.getPartState(state, models.get(i), i), format, bakedTextureGetter);

				if (model.isPresent()) {
                    builder.add(model.get(), (variants.get(i)).getWeight());
				}
			}

			return Optional.of(builder.build());
		}
	}

	public static Optional<ICADModel> onMultipartBlock(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter,
													   ResourceLocation location, Multipart multipart, ImmutableMap<Selector, IModel> partModels) {
		MultipartCADModel.Builder builder = new MultipartCADModel.Builder();
		Iterator iterSelectors = multipart.getSelectors().iterator();

		while (iterSelectors.hasNext()) {
			Selector selector = (Selector) iterSelectors.next();
			Optional<ICADModel> model = callGetModel(partModels.get(selector), partModels.get(selector).getDefaultState(), format, bakedTextureGetter);

			if (model.isPresent()) {
				builder.putModel(selector.getPredicate(multipart.getStateContainer()), model.get());
			}
		}

		return Optional.of(builder.makeMultipartModel());
	}

	public static Optional<ICADModel> callGetModel(IModel model, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		try {
			if (getModelField == null) {
                getModelField = IModel.class.getMethod("getModel", IModelState.class, VertexFormat.class, Function.class);
			}

			return (Optional<ICADModel>) getModelField.invoke(model, state, format, bakedTextureGetter);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

		return Optional.empty();
	}

	public static RegistrySimple<ModelResourceLocation, ICADModel> getModelRegistry() {
		if (modelRegistry == null) {
			modelRegistry = new RegistrySimple<>();

			for (Map.Entry<ModelResourceLocation, IModel> e : stateModels.entrySet()) {
				if (loadedModels.containsKey(e.getValue())) {
					modelRegistry.putObject(e.getKey(), loadedModels.get(e.getValue()));
				}
			}
		}

		return modelRegistry;
	}

	/**
	 * Return a list of classes that implements the IClassTransformer interface
	 *
	 * @return a list of classes that implements the IClassTransformer interface
	 */
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{"de.jdcware.minecad.core.asm.ModelBakeryTransformer"};
	}

	/**
	 * Return a class name that implements "ModContainer" for injection into the mod list
	 * The "getName" function should return a name that other mods can, if need be,
	 * depend on.
	 * Trivially, this modcontainer will be loaded before all regular mod containers,
	 * which means it will be forced to be "immutable" - not susceptible to normal
	 * sorting behaviour.
	 * All other mod behaviours are available however- this container can receive and handle
	 * normal loading events
	 */
	@Override
	public String getModContainerClass() {
		return null;
	}

	/**
	 * Return the class name of an implementor of "IFMLCallHook", that will be run, in the
	 * main thread, to perform any additional setup this coremod may require. It will be
	 * run <strong>prior</strong> to Minecraft starting, so it CANNOT operate on minecraft
	 * itself. The game will deliberately crash if this code is detected to trigger a
	 * minecraft class loading
	 * TODO: implement crash ;)
	 */
	@Nullable
	@Override
	public String getSetupClass() {
		return null;
	}

	/**
	 * Inject coremod data into this coremod
	 * This data includes:
	 * "mcLocation" : the location of the minecraft directory,
	 * "coremodList" : the list of coremods
	 * "coremodLocation" : the file this coremod loaded from,
	 *
	 * @param data
	 */
	@Override
	public void injectData(Map<String, Object> data) {

	}

	/**
	 * Return an optional access transformer class for this coremod. It will be injected post-deobf
	 * so ensure your ATs conform to the new srgnames scheme.
	 *
	 * @return the name of an access transformer class or null if none is provided
	 */
	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
