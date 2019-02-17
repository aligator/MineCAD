package de.jdcware.minecad;

import de.jdcware.minecad.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * MineCAD is a mod for exporting parts of the minecraft world for 3D Printing.
 * Currently it exports the blocks to OpenSCAD.
 * <p>
 * NOT COMPATIBLE WITH CTM-MC
 */
@Mod(modid = MineCAD.MODID, version = MineCAD.VERSION, name = MineCAD.NAME, useMetadata = true)
public class MineCAD {
	public static final String MODID = "minecad";
	public static final String VERSION = "1.12.2-0.0.0.1";
	public static final String NAME = "MineCAD";
	public static final String CLIENT_PROXY = "de.jdcware.minecad.proxy.ClientProxy";
	public static final String COMMON_PROXY = "de.jdcware.minecad.proxy.CommonProxy";
	public static final Logger LOGGER = LogManager.getLogger(MineCAD.MODID);

	@Mod.Instance
	public static MineCAD instance;

	@SidedProxy(clientSide = MineCAD.CLIENT_PROXY, serverSide = MineCAD.COMMON_PROXY)
	public static CommonProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		proxy.serverStarting(event);
	}

	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event) {
		proxy.serverStopping(event);
	}

}
