package de.jdcware.minecad.scanner.scad;

import de.jdcware.minecad.core.BaseCADBuilder;
import de.jdcware.minecad.core.CADRotation;
import eu.printingin3d.javascad.coords.Angles3d;
import eu.printingin3d.javascad.coords.Coords3d;
import eu.printingin3d.javascad.coords.Dims3d;
import eu.printingin3d.javascad.enums.AlignType;
import eu.printingin3d.javascad.enums.Side;
import eu.printingin3d.javascad.models.Abstract3dModel;
import eu.printingin3d.javascad.models.Cube;
import eu.printingin3d.javascad.tranzitions.Rotate;
import eu.printingin3d.javascad.tranzitions.Union;
import net.minecraft.client.renderer.block.model.BlockPartRotation;
import net.minecraft.util.EnumFacing;
import org.lwjgl.util.vector.Vector3f;

import java.util.List;

public class ScadCADBuilder extends BaseCADBuilder<Abstract3dModel> {

    protected ScadCADBuilder() {
        super(Abstract3dModel.class);
    }

    @Override
    public Abstract3dModel quad(Vector3f from, Vector3f to, BlockPartRotation rotation) {

        float xSize = to.x - from.x;
        float ySize = to.y - from.y;
        float zSize = to.z - from.z;

        /*
        if (xSize < minSize) {
            xSize = minSize;
        }

        if (ySize < minSize) {
            ySize = minSize;
        }

        if (zSize < minSize) {
            zSize = minSize;
        }

*/
        double blockOverhang = 0.1;
        // add the block overhang to each block
        xSize += blockOverhang;
        ySize += blockOverhang;
        zSize += blockOverhang;

        // build the cube and rotate, move it to the final position
        Abstract3dModel modelPartCube = new Cube(new Dims3d(xSize, zSize, ySize))
                .align(new Side(AlignType.MIN_IN, AlignType.MIN_IN, AlignType.MIN_IN), new Coords3d(0, 0, 0))
                .move(new Coords3d(from.x - 8, from.z - 8, from.y - 8));

        if (rotation != null) {
            float xRot = 0;
            float yRot = 0;
            float zRot = 0;

            if (rotation.axis == EnumFacing.Axis.X) {
                xRot = -rotation.angle;
            }

            if (rotation.axis == EnumFacing.Axis.Y) {
                yRot = -rotation.angle;
            }

            if (rotation.axis == EnumFacing.Axis.Z) {
                zRot = -rotation.angle;
            }

            // move it so that the origin is in the center of scad.
            modelPartCube = modelPartCube.move(new Coords3d(
                    -16 * rotation.origin.getX() + 8,
                    -16 * rotation.origin.getZ() + 8,
                    -16 * rotation.origin.getY() + 8));

            // rotate the part and move it back to the correct position
            modelPartCube = new Rotate(modelPartCube, new Angles3d(xRot, zRot, yRot)).move(new Coords3d(
                    16 * rotation.origin.getX() - 8,
                    16 * rotation.origin.getZ() - 8,
                    16 * rotation.origin.getY() - 8));
        }


        // combine all blocks and rotate the whole block to the final position.
        return modelPartCube;
    }

    @Override
    public Abstract3dModel union(List<Abstract3dModel> models) {
        return new Union(models);
    }

    @Override
    public Abstract3dModel rotate(Abstract3dModel model, CADRotation blockRotation) {
        return model.rotate(new Angles3d(blockRotation.getRotationX(), 0, blockRotation.getRotationY()));
    }
}
