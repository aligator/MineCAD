package de.jdcware.minecad.item;

import de.jdcware.minecad.MineCAD;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

public class ItemBase extends Item {

	protected String name;

	public ItemBase(String name, CreativeTabs tab, int maxStack) {
		this.name = name;

		setRegistryName(MineCAD.MODID, name);
		final ResourceLocation registryName = Objects.requireNonNull(getRegistryName());
		setUnlocalizedName(registryName.toString());

		setCreativeTab(tab);
		setMaxStackSize(maxStack);
	}

	public void registerItemModel() {
		MineCAD.proxy.registerItemRenderer(this, 0, name);
	}
}
