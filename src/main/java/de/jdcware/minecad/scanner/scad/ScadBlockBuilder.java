package de.jdcware.minecad.scanner.scad;

import com.google.common.collect.Lists;
import de.jdcware.minecad.core.CADPoint;
import de.jdcware.minecad.core.CADQuad;
import de.jdcware.minecad.core.ICADModel;
import de.jdcware.minecad.scanner.IModelBuilder;
import eu.printingin3d.javascad.coords.Angles3d;
import eu.printingin3d.javascad.coords.Coords3d;
import eu.printingin3d.javascad.coords.Triangle3d;
import eu.printingin3d.javascad.models.Abstract3dModel;
import eu.printingin3d.javascad.models.Polyhedron;
import eu.printingin3d.javascad.tranzitions.Rotate;
import eu.printingin3d.javascad.tranzitions.Union;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

/**
 * This is for building a SCad model out of the minecraft models.
 */
public class ScadBlockBuilder implements IModelBuilder<Abstract3dModel> {

	private final float blockOverhang;
	private final float minSize;
	private final List<Abstract3dModel> models = new ArrayList<>();
	private final long rand;

	/**
	 * @param blockOverhang Each block is created bigger by this value. This leads to little overlapping betweend the blocks and as a result prevents non-manifold models, which are very bad for 3d printing.
	 * @param minSize       Minimal size for a block
	 */
	public ScadBlockBuilder(float blockOverhang, float minSize, long rand) {
		this.blockOverhang = blockOverhang;
		this.minSize = minSize;
		this.rand = rand;
	}

	/**
	 * add a ModelBlock to the scad model.
	 * @param cadModel
	 */
	public void add(ICADModel cadModel, IBlockState state) {
		List<Abstract3dModel> blocks = new ArrayList<>();
		Angles3d rotation = new Angles3d(cadModel.getRotation().getRotationX(), cadModel.getRotation().getRotationY(), 0);

		for (CADQuad quad : cadModel.getQuads(state, EnumFacing.EAST, rand)) {
			List<CADPoint> points = quad.getPoints();

			List<Triangle3d> triangles = Lists.newArrayList();

			// bottom
			triangles.add(createTriangle(points, 0, 1, 2));
			triangles.add(createTriangle(points, 0, 2, 3));

			// front
			triangles.add(createTriangle(points, 0, 1, 5));
			triangles.add(createTriangle(points, 0, 5, 4));

			// top
			triangles.add(createTriangle(points, 4, 5, 6));
			triangles.add(createTriangle(points, 4, 6, 7));

			// right
			triangles.add(createTriangle(points, 1, 2, 6));
			triangles.add(createTriangle(points, 1, 6, 5));

			// back
			triangles.add(createTriangle(points, 2, 3, 7));
			triangles.add(createTriangle(points, 2, 7, 6));

			// left
			triangles.add(createTriangle(points, 3, 0, 4));
			triangles.add(createTriangle(points, 3, 4, 7));

			Abstract3dModel modelPartCube = new Polyhedron(triangles);

			if (quad.getRotation() != null) {
				float xRot = 0;
				float yRot = 0;
				float zRot = 0;

				if (quad.getRotation().axis == EnumFacing.Axis.X) {
					xRot = -quad.getRotation().angle;
				}

				if (quad.getRotation().axis == EnumFacing.Axis.Y) {
					yRot = -quad.getRotation().angle;
				}

				if (quad.getRotation().axis == EnumFacing.Axis.Z) {
					zRot = -quad.getRotation().angle;
				}

				// move it so that the origin is in the center of scad.
				modelPartCube = modelPartCube.move(new Coords3d(
						-16 * quad.getRotation().origin.getX() + 8,
						-16 * quad.getRotation().origin.getZ() + 8,
						-16 * quad.getRotation().origin.getY() + 8));

				// rotate the part and move it back to the correct position
				modelPartCube = new Rotate(modelPartCube, new Angles3d(xRot, zRot, yRot)).move(new Coords3d(
						16 * quad.getRotation().origin.getX() - 8,
						16 * quad.getRotation().origin.getZ() - 8,
						16 * quad.getRotation().origin.getY() - 8));
			}


			blocks.add(modelPartCube);
		}

		models.add(new Union(blocks).rotate(rotation));
	}

	private Triangle3d createTriangle(List<CADPoint> points, int index0, int index1, int index2) {
		Triangle3d triangle = new Triangle3d(
				new Coords3d(points.get(index0).x, points.get(index0).y, points.get(index0).z),
				new Coords3d(points.get(index1).x, points.get(index1).y, points.get(index1).z),
				new Coords3d(points.get(index2).x, points.get(index2).y, points.get(index2).z)
		);

		return triangle;
	}
/*
		Angles3d rotation = new Angles3d(modelData.getModelRotation().getRotationX(), 0, modelData.getModelRotation().getRotationY());
		List<Abstract3dModel> blocks = new ArrayList<>();

		for (BlockPart blockPart : modelData.getBlockParts()) {
			Vector3f from = blockPart.positionFrom;
			Vector3f to = blockPart.positionTo;

			float xSize = to.x - from.x;
			float ySize = to.y - from.y;
			float zSize = to.z - from.z;

			if (xSize < minSize) {
				xSize = minSize;
			}

			if (ySize < minSize) {
				ySize = minSize;
			}

			if (zSize < minSize) {
				zSize = minSize;
			}

			// add the block overhang to each block
			xSize += blockOverhang;
			ySize += blockOverhang;
			zSize += blockOverhang;

			// build the cube and rotate, move it to the final position
			Abstract3dModel modelPartCube = new Cube(new Dims3d(xSize, zSize, ySize))
					.align(new Side(AlignType.MIN_IN, AlignType.MIN_IN, AlignType.MIN_IN), new Coords3d(0, 0, 0))
					.move(new Coords3d(from.x - 8, from.z - 8, from.y - 8));

			if (blockPart.partRotation != null) {
				float xRot = 0;
				float yRot = 0;
				float zRot = 0;

				if (blockPart.partRotation.axis == EnumFacing.Axis.X) {
					xRot = -blockPart.partRotation.angle;
				}

				if (blockPart.partRotation.axis == EnumFacing.Axis.Y) {
					yRot = -blockPart.partRotation.angle;
				}

				if (blockPart.partRotation.axis == EnumFacing.Axis.Z) {
					zRot = -blockPart.partRotation.angle;
				}

				// move it so that the origin is in the center of scad.
				modelPartCube = modelPartCube.move(new Coords3d(
						-16 * blockPart.partRotation.origin.getX() + 8,
						-16 * blockPart.partRotation.origin.getZ() + 8,
						-16 * blockPart.partRotation.origin.getY() + 8));

				// rotate the part and move it back to the correct position
				modelPartCube = new Rotate(modelPartCube, new Angles3d(xRot, zRot, yRot)).move(new Coords3d(
						16 * blockPart.partRotation.origin.getX() - 8,
						16 * blockPart.partRotation.origin.getZ() - 8,
						16 * blockPart.partRotation.origin.getY() - 8));
			}

			blocks.add(modelPartCube);
		}

		// combine all blocks and rotate the whole block to the final position.
		models.add(new Union(blocks).rotate(rotation));*/
	//}

	@Override
	// return all models as one Union
	public Abstract3dModel build() {
		return new Union(models);
	}
}
