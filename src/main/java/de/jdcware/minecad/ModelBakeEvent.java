package de.jdcware.minecad;

import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

public class ModelBakeEvent extends Event {

	private final List<BlockPart> elements;
	private final ModelResourceLocation location;

	public ModelBakeEvent(Object elements, Object location) {
		this.elements = (List<BlockPart>) elements;
		this.location = (ModelResourceLocation) location;
	}

	public List<BlockPart> getElements() {
		return this.elements;
	}

	public ModelResourceLocation getLocation() {
		return this.location;
	}
}
