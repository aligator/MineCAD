package de.jdcware.minecad.core;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IModel;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimpleCADModel extends BaseCADModel {

    private final List<CADQuad> quads;

    public SimpleCADModel(IModel model) {
        this(model, null);
    }

    public SimpleCADModel(IModel model, ModelRotation rotation) {
        super(rotation);

        quads = new ArrayList<>();

        Optional<ModelBlock> vanillaModel = model.asVanillaModel();
        if (vanillaModel.isPresent()) {


            // TODO: universal way to save sizes of cubes.
            //       Possible:
            //		 from, to --> only cubes possible, better performance
            for (BlockPart part : vanillaModel.get().getElements()) {
                List<CADPoint> points = new ArrayList<>();
                Vector3f from = part.positionFrom;
                Vector3f to = part.positionTo;


                points.add(new CADPoint(from.x, from.y, from.z));
                points.add(new CADPoint(from.x + to.x, from.y, from.z));
                points.add(new CADPoint(from.x + to.x, from.y + to.y, from.z));
                points.add(new CADPoint(from.x, from.y + to.y, from.z));

                points.add(new CADPoint(from.x, from.y, from.z + to.z));
                points.add(new CADPoint(from.x + to.x, from.y, from.z + to.z));
                points.add(new CADPoint(from.x + to.x, from.y + to.y, from.z + to.z));
                points.add(new CADPoint(from.x, from.y + to.y, from.z + to.z));

                quads.add(new CADQuad(points, part.partRotation));
            }


        }
    }

    public List<CADQuad> getQuads(IBlockState state, EnumFacing facing, long rand) {
        return quads;
    }
}
