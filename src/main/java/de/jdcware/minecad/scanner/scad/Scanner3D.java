package de.jdcware.minecad.scanner.scad;

import de.jdcware.minecad.MineCAD;
import de.jdcware.minecad.MineCADConfig;
import de.jdcware.minecad.scanner.IBlockData;
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
						IBlockData mcBlockModelData = null;

						BlockModelShapes modelShapes = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
						BlockStateMapper blockstatemapper = modelShapes.getBlockStateMapper();

						// TODO: find a way to know which resource location should be used. (example: sapplings)
						//       it now uses just the first one
						for (final ResourceLocation resourcelocation : blockstatemapper.getBlockstateLocations(blockState.getBlock())) {
							mcBlockModelData = MineCAD.modelRegistry.getObject(new ModelResourceLocation(resourcelocation, modelShapes.getBlockStateMapper().getVariants(blockState.getBlock()).get(blockState).getVariant()));
							break;
						}

						// if no block found use the standard minecraft-block as fall back
						if (mcBlockModelData == null) {
							mcBlockModelData = MineCAD.modelRegistry.getObject(new ModelResourceLocation(new ResourceLocation("minecraft", "cobblestone"), "normal"));
							MineCAD.LOGGER.info("block " + currentBlock.getRegistryName() + " has no 3d-data in resources.");
						}

						Abstract3dModel model = (Abstract3dModel) mcBlockModelData.getBlockModelData(blockState);
						// add the model and move it to the correct position
						models.add(model.move(new Coords3d(i * 16, k * 16, j * 16)));
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