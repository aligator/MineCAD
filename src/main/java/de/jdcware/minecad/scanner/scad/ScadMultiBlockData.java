package de.jdcware.minecad.scanner.scad;

import com.google.common.base.Predicate;
import de.jdcware.minecad.scanner.IBlockData;
import eu.printingin3d.javascad.models.Abstract3dModel;
import eu.printingin3d.javascad.tranzitions.Union;
import net.minecraft.block.state.IBlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScadMultiBlockData implements IBlockData<Abstract3dModel> {

	private final Map<Predicate<IBlockState>, Abstract3dModel> selectors;

	ScadMultiBlockData(Map<Predicate<IBlockState>, Abstract3dModel> selectors) {
		this.selectors = selectors;
	}

	@Override
	public Abstract3dModel getBlockParts(IBlockState state) {
		List<Abstract3dModel> list = new ArrayList<>();

		if (state != null) {
			for (Map.Entry<Predicate<IBlockState>, Abstract3dModel> entry : this.selectors.entrySet()) {
				if ((entry.getKey()).apply(state)) {
					list.add(entry.getValue());
				}
			}
		}

		return new Union(list);
	}
}