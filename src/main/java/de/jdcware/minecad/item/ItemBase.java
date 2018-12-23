package de.jdcware.minecad.item;

import de.jdcware.minecad.MineCAD;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemBase extends Item {

	public ItemBase(String registryName, String name, CreativeTabs tab, int maxStack) {
		setRegistryName(MineCAD.MODID, registryName);
		setUnlocalizedName(name);

		setCreativeTab(tab);
		setMaxStackSize(maxStack);
	}

	//public void registerItemModel() {
	//	MineCAD.proxy.registerItemRenderer(this, 0, name);
	//}
}
