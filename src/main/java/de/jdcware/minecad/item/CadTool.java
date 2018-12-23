package de.jdcware.minecad.item;

import de.jdcware.minecad.scad.Scanner3D;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CadTool extends ItemBase {
	public CadTool(CreativeTabs tab, int maxStack) {
		super("cadtool", "Cad Tool", tab, maxStack);
	}

	@Override
	@MethodsReturnNonnullByDefault
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		//MineCAD.LOGGER.info(worldIn.getBlockState(pos).getBlock().getClass());

		Scanner3D scanner = new Scanner3D(pos, pos.add(20, 20, 20), 0.0f, 1);
		scanner.scan(worldIn);

		return EnumActionResult.SUCCESS;
	}
}
