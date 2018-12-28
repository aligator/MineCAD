package de.jdcware.minecad.item;

import de.jdcware.minecad.scanner.scad.Scanner3D;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The CAD tool is used for defining the area for the 3d-export.
 * You have to select two blocks with a right click.
 * After the seccond click the blocks in the defined area are being exported.
 */
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
		if (worldIn.isRemote) { // only on the client
			if (firstClick) {
				firstPos = pos;
			} else {
				Tuple<BlockPos, BlockPos> minMax = getMinMax(firstPos, pos);

				Scanner3D scanner = new Scanner3D(minMax.getFirst(), minMax.getFirst().add(
						minMax.getSecond().getX() - minMax.getFirst().getX() + 1,
						minMax.getSecond().getY() - minMax.getFirst().getY() + 1,
						minMax.getSecond().getZ() - minMax.getFirst().getZ() + 1));
				scanner.scan(worldIn);

			}

			firstClick = !firstClick;
		}

		return EnumActionResult.SUCCESS;
	}

	/**
	 * get the min and max position. Each axis is looked at individually.
	 *
	 * @param pos1
	 * @param pos2
	 * @return A Tuple with the min-position as first value and the max position as seccond value.
	 */
	private Tuple<BlockPos, BlockPos> getMinMax(BlockPos pos1, BlockPos pos2) {
		Tuple<Double, Double> x = getMinMax(pos1.getX(), pos2.getX());
		Tuple<Double, Double> y = getMinMax(pos1.getY(), pos2.getY());
		Tuple<Double, Double> z = getMinMax(pos1.getZ(), pos2.getZ());

		return new Tuple<>(
				new BlockPos(x.getFirst(), y.getFirst(), z.getFirst()),
				new BlockPos(x.getSecond(), y.getSecond(), z.getSecond()));

	}

	/**
	 * get which of the two provided vlues is min and which is max.
	 *
	 * @param val1
	 * @param val2
	 * @return A Tuple with the min-value as first and max-value as seccond value.
	 */
	private Tuple<Double, Double> getMinMax(double val1, double val2) {
		if (val1 < val2) {
			return new Tuple<>(val1, val2);
		}

		return new Tuple<>(val2, val1);
	}
}
