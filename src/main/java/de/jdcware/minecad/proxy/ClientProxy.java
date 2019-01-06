package de.jdcware.minecad.proxy;

import de.jdcware.minecad.MineCAD;
import de.jdcware.minecad.MineCADConfig;
import de.jdcware.minecad.core.asm.MineCADCorePlugin;
import de.jdcware.minecad.scanner.scad.ScadBlockBuilder;
import de.jdcware.minecad.scanner.scad.ScadBlockData;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);

		//ScadModelBakery bakery = ScadModelBakery.getInstance(MineCADConfig.blockOverhang, MineCADConfig.minSize);

		MineCAD.modelRegistry = new RegistrySimple<>();

		for (ModelResourceLocation location : MineCADCorePlugin.getModelRegistry().getKeys()) {
			ScadBlockBuilder builder = new ScadBlockBuilder(MineCADConfig.blockOverhang, MineCADConfig.minSize);
			builder.add(MineCADCorePlugin.getModelRegistry().getObject(location));
			MineCAD.modelRegistry.putObject(location, new ScadBlockData(builder.build()));
		}

	}

	@Override
	public void serverStarting(FMLServerStartingEvent event) {
		super.serverStarting(event);
	}

	@Override
	public void serverStopping(FMLServerStoppingEvent event) {
		super.serverStopping(event);
	}
}

