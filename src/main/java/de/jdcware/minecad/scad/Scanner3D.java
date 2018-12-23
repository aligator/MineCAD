package de.jdcware.minecad.scad;

import de.jdcware.minecad.MineCAD;
import eu.printingin3d.javascad.coords.Angles3d;
import eu.printingin3d.javascad.coords.Coords3d;
import eu.printingin3d.javascad.coords.Dims3d;
import eu.printingin3d.javascad.enums.AlignType;
import eu.printingin3d.javascad.enums.Side;
import eu.printingin3d.javascad.models.Abstract3dModel;
import eu.printingin3d.javascad.models.Cube;
import eu.printingin3d.javascad.tranzitions.Mirror;
import eu.printingin3d.javascad.tranzitions.Rotate;
import eu.printingin3d.javascad.tranzitions.Union;
import eu.printingin3d.javascad.utils.SaveScadFiles;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
public class Scanner3D {

	private BlockPos p1;
	private BlockPos p2;
	private int minSize;
	private float blockOverhang;

	public Scanner3D(BlockPos p1, BlockPos p2, float blockOverhang, int minSize) {
		this.p1 = p1;
		this.p2 = p2;
		this.minSize = minSize;
		this.blockOverhang = blockOverhang;
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
					MineCAD.LOGGER.info(current.toString());

					IBlockState blockState = world.getBlockState(current);

					if (world.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
						blockState = blockState.getActualState(world, current);
					}

					Block currentBlock = blockState.getBlock();

					if (!currentBlock.isAir(blockState, world, current)) {
						EnumFacing facing = EnumFacing.EAST;
						BlockStairs.EnumHalf half = BlockStairs.EnumHalf.BOTTOM;

						BlockData mcBlockModelData = null;

						try {
							mcBlockModelData = ResourceParser.parse(blockState);
						} catch (Exception ex) {
							ex.printStackTrace();
						}

						if (mcBlockModelData != null) {
							MineCAD.LOGGER.info("block " + currentBlock.getRegistryName() + " loaded.");

							models.add(cube(mcBlockModelData, facing, half).move(new Coords3d(i * 16, k * 16, j * 16)));

						} else {
							MineCAD.LOGGER.info("block " + currentBlock.getRegistryName() + " has no 3d-data in resources.");
						}
					} else {
						MineCAD.LOGGER.info("block " + currentBlock.getRegistryName() + " is ignored.");
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


	private Abstract3dModel cube(BlockData modelBlock, EnumFacing facing, BlockStairs.EnumHalf half) {
		List<Abstract3dModel> models = new ArrayList<>();

		Angles3d rotation = new Angles3d(modelBlock.getXRotation(), 0, modelBlock.getYRotation());

		for (BlockPart blockPart : modelBlock.getBlockParts()) {
			Vector3f from = blockPart.positionFrom;
			Vector3f to = blockPart.positionTo;

			float xSize = to.x - from.x;
			float ySize = to.y - from.y;
			float zSize = to.z - from.z;

			if (xSize < minSize)
				xSize = minSize;

			if (ySize < minSize)
				ySize = minSize;

			if (zSize < minSize)
				zSize = minSize;

			xSize += blockOverhang;
			ySize += blockOverhang;
			zSize += blockOverhang;

			Abstract3dModel modelPartCube = new Cube(new Dims3d(xSize, zSize, ySize))
					.align(new Side(AlignType.MIN_IN, AlignType.MIN_IN, AlignType.MIN_IN), new Coords3d(0, 0, 0))
					.move(new Coords3d(from.x - 8, from.z - 8, from.y - 8));

			if (blockPart.partRotation != null) {
				float xRot = 0;
				float yRot = 0;
				float zRot = 0;

				if (blockPart.partRotation.axis == EnumFacing.Axis.X)
					xRot = -blockPart.partRotation.angle;

				if (blockPart.partRotation.axis == EnumFacing.Axis.Y)
					yRot = -blockPart.partRotation.angle;

				if (blockPart.partRotation.axis == EnumFacing.Axis.Z)
					zRot = -blockPart.partRotation.angle;


				modelPartCube = modelPartCube.move(new Coords3d(
						-16 * blockPart.partRotation.origin.getX() + 8,
						-16 * blockPart.partRotation.origin.getZ() + 8,
						-16 * blockPart.partRotation.origin.getY() + 8));

				modelPartCube = new Rotate(modelPartCube, new Angles3d(xRot, zRot, yRot)).move(new Coords3d(
						16 * blockPart.partRotation.origin.getX() - 8,
						16 * blockPart.partRotation.origin.getZ() - 8,
						16 * blockPart.partRotation.origin.getY() - 8));


                /*modelPartCube = modelPartCube.move(new Coords3d(16 * blockPart.partRotation.origin.getX(),
                                                                 16 * blockPart.partRotation.origin.getZ(),
                                                                 16 * blockPart.partRotation.origin.getY()));*/
				//.rotate(new Angles3d(xRot, zRot, yRot));*/

			}

			models.add(modelPartCube);

			//
			//.move(translation));
		}

		return new Union(models).rotate(rotation);
	}
}
