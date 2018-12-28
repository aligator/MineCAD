package de.jdcware.minecad;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;

@Config(modid = MineCAD.MODID)
public class MineCADConfig {

	@Comment("The folder the model should be exported to.")
	@Name("filepath")
	public static String filepath = "/tmp/";

	@Comment("The file name for the exported model.")
	@Name("filename")
	public static String filename = "out.scad";

	@Comment("Each block is created bigger by this value. \nThis leads to little overlapping betweend the blocks and as a result prevents non-manifold models, \nwhich are very bad for 3d printing.")
	@Name("block_overhang")
	public static float blockOverhang = 0.1f;

	@Comment("Some models in minecraft are flat. This is not printable. So you can define a min size for these cases.")
	@Name("min_size")
	public static float minSize = 1;

	@Comment("A list for ignored blocks. Uses the internal name.")
	@Name("ignored_blocks")
	public static String[] ignoredBlocks = {"minecraft:glass"};

	/**
	 * check if a block is in the ignored list.
	 *
	 * @param regName
	 * @return true or false
	 */
	public static boolean isIgnoredBlock(ResourceLocation regName) {
		for (String ignoredBlock : ignoredBlocks) {
			if ((regName.getResourceDomain() + ":" + regName.getResourcePath()).equals(ignoredBlock)) {
				return true;
			}
		}
		return false;
	}
}
