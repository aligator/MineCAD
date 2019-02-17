package de.jdcware.minecad.scanner.scad;

import de.jdcware.minecad.MineCADConfig;
import de.jdcware.minecad.core.ICADModel;
import de.jdcware.minecad.core.asm.MineCADCorePlugin;
import eu.printingin3d.javascad.coords.Coords3d;
import eu.printingin3d.javascad.models.Abstract3dModel;
import eu.printingin3d.javascad.tranzitions.Mirror;
import eu.printingin3d.javascad.tranzitions.Union;
import eu.printingin3d.javascad.utils.SaveScadFiles;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The scanner scans each block between the provided positions and builds a 3D model out of it.
 */
public class Scanner3D {

	private BlockPos p1;
	private BlockPos p2;

	public Scanner3D(BlockPos p1, BlockPos p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	public void scan(World world) {
		BlockPos current = p1;

		int dirX = -Integer.compare(p1.getX(), p2.getX());
		int dirY = -Integer.compare(p1.getY(), p2.getY());
		int dirZ = -Integer.compare(p1.getZ(), p2.getZ());

		int xLength = dirX * (p2.getX() - p1.getX());
		int yLength = dirY * (p2.getY() - p1.getY());
		int zLength = dirZ * (p2.getZ() - p1.getZ());

		List<Abstract3dModel> models = new ArrayList<>();

		ScadCADBuilder builder = new ScadCADBuilder(MineCADConfig.minSize, MineCADConfig.blockOverhang);

		// for each block
		for (int i = 0; i < xLength; i++) {
			for (int j = 0; j < yLength; j++) {
				for (int k = 0; k < zLength; k++) {
					// current is the pointer to the currently used block
					current = p1.add(i, j, k);

					IBlockState blockState = world.getBlockState(current);

					if (world.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
						blockState = blockState.getActualState(world, current);
					}

					Block currentBlock = blockState.getBlock();

					// filter ignored blocks and air
					if (!currentBlock.isAir(blockState, world, current) && !MineCADConfig.isIgnoredBlock(blockState.getBlock().getRegistryName())) {
						ICADModel cadModel = null;

						BlockModelShapes modelShapes = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
						BlockStateMapper blockstatemapper = modelShapes.getBlockStateMapper();

						for (final ResourceLocation resourcelocation : blockstatemapper.getBlockstateLocations(blockState.getBlock())) {

							cadModel = MineCADCorePlugin.getModelRegistry().getObject(new ModelResourceLocation(resourcelocation, modelShapes.getBlockStateMapper().getVariants(blockState.getBlock()).get(blockState).getVariant()));
							break;
						}

						if (cadModel != null) {
							Abstract3dModel model = builder.buildModel(cadModel, blockState, 5);

							// add the model and move it to the correct position
							models.add(model.move(new Coords3d(i * 16, k * 16, j * 16)));
						}
					}
				}
			}
		}

		if (models.size() != 0) {

			// create scad file
			try {
				new SaveScadFiles(new File(MineCADConfig.filepath))
						.addModel(MineCADConfig.filename, Mirror.mirrorY(new Union(models)))
						.saveScadFiles();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
