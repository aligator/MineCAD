package de.jdcware.minecad;

import de.jdcware.minecad.item.ItemBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems {

	public static ItemBase cadTool = new ItemBase("cadtool", CreativeTabs.TOOLS, 64);

	public static void register(IForgeRegistry<Item> registry) {
		registry.registerAll(
				cadTool
		);
	}

	public static void registerModels() {
		cadTool.registerItemModel();
	}
}
