package de.jdcware.minecad.core.asm;

import com.google.common.collect.Maps;
import de.jdcware.minecad.MineCAD;
import de.jdcware.minecad.core.BaseBlockData;
import de.jdcware.minecad.core.IBaseBlockData;
import de.jdcware.minecad.core.ModelRegistryResolver;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@IFMLLoadingPlugin.TransformerExclusions({"de.jdcware.minecad.core.asm"})
@IFMLLoadingPlugin.MCVersion(value = "1.12.2")
public class MineCADCorePlugin implements IFMLLoadingPlugin {

	public static final Logger LOGGER = LogManager.getLogger(MineCAD.MODID);
	public static Map<ModelResourceLocation, IModel> stateModels = Maps.newHashMap();
	public static Map<ModelResourceLocation, ModelBlockDefinition> multipartDefinitions = Maps.newHashMap();
	public static Map<ModelBlockDefinition, IModel> multipartModels = Maps.newHashMap();
	public static RegistrySimple<ModelResourceLocation, IBaseBlockData> modelRegistry;

	private static ModelRegistryResolver resolver = new ModelRegistryResolver();

	public static void onBlocksLoaded(Map<ModelResourceLocation, IModel> stateModels, Map<ModelResourceLocation, ModelBlockDefinition> multipartDefinitions, Map<ModelBlockDefinition, IModel> multipartModels) {
		MineCADCorePlugin.stateModels = stateModels;
		MineCADCorePlugin.multipartDefinitions = multipartDefinitions;
		MineCADCorePlugin.multipartModels = multipartModels;
	}

	public static void onBlockBake(IModel model, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		try {
			Optional<ModelBlock> modelBlock = (Optional<ModelBlock>) ObfuscationReflectionHelper.findMethod(IModel.class, "getModel", void.class, IModelState.class, VertexFormat.class, Function.class).invoke(model, state, format, bakedTextureGetter);
			if (modelBlock.isPresent()) {
				LOGGER.info(modelBlock.get().name);
				resolver.add(model, new BaseBlockData(modelBlock.get(), ModelRotation.X0_Y0));
			}

		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static void onVanillaBlockBake(IModel origin, IModelState state, ResourceLocation location, ModelBlock model, boolean uvlock, ModelBlockAnimation animation) {
		if (origin.asVanillaModel().isPresent()) {
			resolver.add(origin, new BaseBlockData(origin.asVanillaModel().get()));
		} else {
			// Todo: add default block
		}
	}

	public static void onWeightedRandomBlockBake(IModel origin, IModelState state, List<Variant> variants, List<ResourceLocation> locations, Set<ResourceLocation> textures, List<IModel> models, IModelState defaultState) {
		resolver.resolveMissing();

		// TODO: currently only the first variant is used. Random on every use for example...
		if (variants.size() > 0) {
			// TODO: The ModelLoader loads all variants. But we only load the first.
			resolver.addWithDependency(origin, variants);
			return;
		}
	}

	public static Optional<ModelBlock> callGetModel(IModel model, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		try {
			return (Optional<ModelBlock>) ObfuscationReflectionHelper.findMethod(IModel.class, "getModel", void.class, IModelState.class, VertexFormat.class, Function.class)
					.invoke(model, state, format, bakedTextureGetter);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	public static RegistrySimple<ModelResourceLocation, IBaseBlockData> getModelRegistry() {
		if (modelRegistry == null) {
			modelRegistry = resolver.getModelRegistry(stateModels);
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
