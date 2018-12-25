package de.jdcware.minecad.scanner;

import de.jdcware.minecad.MineCAD;
import eu.printingin3d.javascad.coords.Coords3d;
import eu.printingin3d.javascad.models.Abstract3dModel;
import eu.printingin3d.javascad.tranzitions.Mirror;
import eu.printingin3d.javascad.tranzitions.Union;
import eu.printingin3d.javascad.utils.SaveScadFiles;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Scanner3D {

	private BlockPos p1;
	private BlockPos p2;
	private int minSize;
	private float blockOverhang;
	private IBlockDataLoader blockDataLoader;

	public Scanner3D(BlockPos p1, BlockPos p2, float blockOverhang, int minSize) {
		this.p1 = p1;
		this.p2 = p2;
		this.minSize = minSize;
		this.blockOverhang = blockOverhang;
		this.blockDataLoader = new ResourceParser();
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

		for (int i = 0; i < xLength; i++) {
			for (int j = 0; j < yLength; j++) {
				for (int k = 0; k < zLength; k++) {
					current = p1.add(i, j, k);

					IBlockState blockState = world.getBlockState(current);

					if (world.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
						blockState = blockState.getActualState(world, current);
					}

					Block currentBlock = blockState.getBlock();

					if (!currentBlock.isAir(blockState, world, current)) {
						IBlockData mcBlockModelData = null;

						try {
							mcBlockModelData = blockDataLoader.loadBlockData(blockState);
						} catch (Exception ex) {
							ex.printStackTrace();
						}

						if (mcBlockModelData != null) {

							Abstract3dModel model = (Abstract3dModel) mcBlockModelData.getBlockParts(blockState);
							models.add(model.move(new Coords3d(i * 16, k * 16, j * 16)));
							//models.add(cube(mcBlockModelData, facing, half).move(new Coords3d(i * 16, k * 16, j * 16)));

						} else {
							MineCAD.LOGGER.info("block " + currentBlock.getRegistryName() + " has no 3d-data in resources.");
						}
					}
				}
			}
		}

		if (models.size() != 0) {

			try {
				new SaveScadFiles(new File("/tmp"))
						.addModel("out.scad", Mirror.mirrorY(new Union(models)))
						.saveScadFiles();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
