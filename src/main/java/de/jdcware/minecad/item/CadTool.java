package de.jdcware.minecad.item;

import de.jdcware.minecad.MineCAD;
import de.jdcware.minecad.scanner.Scanner3D;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CadTool extends ItemBase {
	private boolean firstClick;
	private BlockPos firstPos;

	public CadTool(CreativeTabs tab, int maxStack) {
		super("cadtool", "Cad Tool", tab, maxStack);
		firstClick = true;
	}

	@Override
	@MethodsReturnNonnullByDefault
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			if (firstClick) {
				firstClick = false;
				firstPos = pos;
			} else {
				firstClick = true;

				MineCAD.LOGGER.info("first: " + firstPos.toString());
				MineCAD.LOGGER.info("seccond: " + pos.toString());

				Tuple<BlockPos, BlockPos> minMax = getMinMax(firstPos, pos);

				Scanner3D scanner = new Scanner3D(minMax.getFirst(), minMax.getFirst().add(
						minMax.getSecond().getX() - minMax.getFirst().getX() + 1,
						minMax.getSecond().getY() - minMax.getFirst().getY() + 1,
						minMax.getSecond().getZ() - minMax.getFirst().getZ() + 1), 0.1f, 1);

				//pos.getDistance()

				scanner.scan(worldIn);

			}
		}

		return EnumActionResult.SUCCESS;
	}

	private Tuple<BlockPos, BlockPos> getMinMax(BlockPos pos1, BlockPos pos2) {
		Tuple<Double, Double> x = getMinMax(pos1.getX(), pos2.getX());
		Tuple<Double, Double> y = getMinMax(pos1.getY(), pos2.getY());
		Tuple<Double, Double> z = getMinMax(pos1.getZ(), pos2.getZ());

		return new Tuple<>(
				new BlockPos(x.getFirst(), y.getFirst(), z.getFirst()),
				new BlockPos(x.getSecond(), y.getSecond(), z.getSecond()));

	}

	private Tuple<Double, Double> getMinMax(double val1, double val2) {
		if (val1 < val2) {
			return new Tuple<>(val1, val2);
		}

		return new Tuple<>(val2, val1);
	}
}
