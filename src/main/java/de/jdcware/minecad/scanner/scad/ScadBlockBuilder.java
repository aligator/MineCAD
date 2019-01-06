package de.jdcware.minecad.scanner.scad;

import de.jdcware.minecad.core.IBaseBlockData;
import de.jdcware.minecad.scanner.IModelBuilder;
import eu.printingin3d.javascad.coords.Angles3d;
import eu.printingin3d.javascad.coords.Coords3d;
import eu.printingin3d.javascad.coords.Dims3d;
import eu.printingin3d.javascad.enums.AlignType;
import eu.printingin3d.javascad.enums.Side;
import eu.printingin3d.javascad.models.Abstract3dModel;
import eu.printingin3d.javascad.models.Cube;
import eu.printingin3d.javascad.tranzitions.Rotate;
import eu.printingin3d.javascad.tranzitions.Union;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.util.vector.Vector3f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * This is for building a SCad model out of the minecraft models.
 */
public class ScadBlockBuilder implements IModelBuilder<Abstract3dModel> {

	private final float blockOverhang;
	private final float minSize;
	private final List<Abstract3dModel> models = new ArrayList<>();
	private final Field quartersXField;
	private final Field quartersYField;

	/**
	 * @param blockOverhang Each block is created bigger by this value. This leads to little overlapping betweend the blocks and as a result prevents non-manifold models, which are very bad for 3d printing.
	 * @param minSize       Minimal size for a block
	 */
	public ScadBlockBuilder(float blockOverhang, float minSize) {
		this.blockOverhang = blockOverhang;
		this.minSize = minSize;

		this.quartersXField = ObfuscationReflectionHelper.findField(ModelRotation.class, "field_177543_t"); // quartersX
		this.quartersYField = ObfuscationReflectionHelper.findField(ModelRotation.class, "field_177542_u"); // quartersY
	}

	/**
	 * add a ModelBlock to the scad model.
	 *
	 * @param blockData a ModelBlock from minecraft
	 */
	public void add(IBaseBlockData blockData) {
		add(blockData.getModelBlock(), blockData.getModelRotation());
	}

	/**
	 * add a ModelBlock to the scad model.
	 * @param modelData a ModelBlock from minecraft
	 * @param modelRotation rotation from the block
	 */
	@Override
	public void add(ModelBlock modelData, ModelRotation modelRotation) {
		Angles3d rotation = new Angles3d(getModelRotationX(modelRotation) * 90, 0, getModelRotationY(modelRotation) * 90);
		List<Abstract3dModel> blocks = new ArrayList<>();

		for (BlockPart blockPart : modelData.getElements()) {
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
		models.add(new Union(blocks).rotate(rotation));
	}

	@Override
	// return all models as one Union
	public Abstract3dModel build() {
		return new Union(models);
	}

	/**
	 * get reflective access to the rotation data of ModelRotation
	 *
	 * @param rotation
	 * @return quartersX
	 */
	private int getModelRotationX(ModelRotation rotation) {
		try {
			return (int) quartersXField.get(rotation);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * get reflective access to the rotation data of ModelRotation
	 *
	 * @param rotation
	 * @return quartersY
	 */
	private int getModelRotationY(ModelRotation rotation) {
		try {
			return (int) quartersYField.get(rotation);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
