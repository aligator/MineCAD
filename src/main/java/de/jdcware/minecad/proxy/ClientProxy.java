package de.jdcware.minecad.proxy;

import de.jdcware.minecad.MineCAD;
import de.jdcware.minecad.MineCADConfig;
import de.jdcware.minecad.TestModel;
import de.jdcware.minecad.TestModel2;
import de.jdcware.minecad.core.asm.MineCADCorePlugin;
import de.jdcware.minecad.scanner.scad.ScadBlockBuilder;
import de.jdcware.minecad.scanner.scad.ScadBlockData;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

import java.util.Optional;

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

		MineCAD.LOGGER.info("test1: ");
		IModel test = new TestModel();
		Optional<ModelBlock> testModel = MineCADCorePlugin.callGetModel(test, null, null, null);
		MineCAD.LOGGER.info("test: ende");

		MineCAD.LOGGER.info("test2: ");
		IModel test2 = new TestModel2();
		Optional<ModelBlock> testModel2 = MineCADCorePlugin.callGetModel(test2, null, null, null);
		MineCAD.LOGGER.info("test2: ende");

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

