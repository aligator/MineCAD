package de.jdcware.minecad.scanner;

import de.jdcware.minecad.MineCAD;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistrySimple;

public class ResourceParser implements IBlockDataLoader {

	@Override
	public IBlockData loadBlockData(IBlockState blockState) {
		RegistrySimple<ModelResourceLocation, IBlockData> scadModelBakery = MineCAD.scadModelBakery.getRegistry();

		BlockModelShapes modelShapes = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
		BlockStateMapper blockstatemapper = modelShapes.getBlockStateMapper();


		for (final ResourceLocation resourcelocation : blockstatemapper.getBlockstateLocations(blockState.getBlock())) {
			return scadModelBakery.getObject(new ModelResourceLocation(resourcelocation, modelShapes.getBlockStateMapper().getVariants(blockState.getBlock()).get(blockState).getVariant()));

		}

		return null;
	}
}

