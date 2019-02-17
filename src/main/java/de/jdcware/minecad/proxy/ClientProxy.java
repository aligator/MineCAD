package de.jdcware.minecad.proxy;

import net.minecraftforge.fml.common.event.*;

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
	/*	MineCAD.modelRegistry = new RegistrySimple<>();

		for (ModelResourceLocation location : MineCADCorePlugin.getModelRegistry().getKeys()) {
			ScadBlockBuilder builder = new ScadBlockBuilder(MineCADConfig.blockOverhang, MineCADConfig.minSize);
			builder.add(MineCADCorePlugin.getModelRegistry().getObject(location));
			MineCAD.modelRegistry.putObject(location, new ScadBlockData(builder.build()));
		}*/
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

