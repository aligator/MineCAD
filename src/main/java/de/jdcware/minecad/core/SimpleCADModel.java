package de.jdcware.minecad.core;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraftforge.client.model.IModel;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimpleCADModel extends BaseCADModel {

    private final List<CADQuad> quads;

    public SimpleCADModel(IModel model, CADRotation rotation) {
        super(rotation);

        quads = new ArrayList<>();

        Optional<ModelBlock> vanillaModel = model.asVanillaModel();
        if (vanillaModel.isPresent()) {
            for (BlockPart part : vanillaModel.get().getElements()) {
                Vector3f from = part.positionFrom;
                Vector3f to = part.positionTo;

                quads.add(new CADQuad(from, to, part.partRotation));
            }


        }
    }

    public Object buildModel(ICADBuilder builder, IBlockState state, long rand) {
        List builtModel = Lists.newArrayList();

        for (CADQuad quad : quads) {
            builtModel.add(builder.quad(quad.getFrom(), quad.getTo(), quad.getRotation()));
        }

        Object fullModel = builder.union(builtModel);

        return builder.rotate(fullModel, getRotation());
    }
}
