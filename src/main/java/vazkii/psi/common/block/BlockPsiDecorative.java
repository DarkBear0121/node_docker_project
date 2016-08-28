/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [31/01/2016, 19:09:11 (GMT)]
 */
package vazkii.psi.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import vazkii.arl.block.BlockMetaVariants;
import vazkii.psi.common.block.base.IPsiBlock;
import vazkii.psi.common.lib.LibBlockNames;

public class BlockPsiDecorative extends BlockMetaVariants implements IPsiBlock {

	public BlockPsiDecorative() {
		super(LibBlockNames.PSI_DECORATIVE, Material.IRON, Variants.class);
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		Variants variant = (Variants) state.getValue(variantProp);
		return variant == Variants.PSIMETAL_PLATE_BLACK_LIGHT || variant == Variants.PSIMETAL_PLATE_CYAN_LIGHT ? 15 : 0;
	}

	private static enum Variants implements EnumBase {
		PSIDUST_BLOCK,
		PSIMETAL_BLOCK,
		PSIGEM_BLOCK,
		PSIMETAL_PLATE_BLACK,
		PSIMETAL_PLATE_BLACK_LIGHT,
		PSIMETAL_PLATE_CYAN,
		PSIMETAL_PLATE_CYAN_LIGHT,
		EBONY_PSIMETAL_BLOCK,
		IVORY_PSIMETAL_BLOCK
	}

}
