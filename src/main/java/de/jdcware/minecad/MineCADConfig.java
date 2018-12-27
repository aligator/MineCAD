package de.jdcware.minecad;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Name;

@Config(modid = MineCAD.MODID)
public class MineCADConfig {

	@Name("filepath")
	public static String filepath = "/tmp/";

	@Name("filename")
	public static String filename = "out.scad";

	@Name("ignored_blocks")
	public static String[] ignoredBlocks = {"minecraft:glass"};

	public static boolean isIgnoredBlock(ResourceLocation regName) {
		for (String ignoredBlock : ignoredBlocks) {
			if ((regName.getResourceDomain() + ":" + regName.getResourcePath()).equals(ignoredBlock)) {
				return true;
			}
		}
		return false;
	}
}
