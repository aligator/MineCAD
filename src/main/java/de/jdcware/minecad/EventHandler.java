package de.jdcware.minecad;

import de.jdcware.minecad.item.CadTool;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EventHandler {

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		CadTool cadTool = new CadTool("cadtool", CreativeTabs.TOOLS, 64);

		event.getRegistry().register(cadTool);
	}

	@SubscribeEvent
	public static void registerItems(ModelRegistryEvent event) {
		ModItems.registerModels();
	}
}
