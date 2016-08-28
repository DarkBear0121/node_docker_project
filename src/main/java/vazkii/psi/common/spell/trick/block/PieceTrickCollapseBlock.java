/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [01/02/2016, 20:14:04 (GMT)]
 */
package vazkii.psi.common.spell.trick.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.common.core.handler.ConfigHandler;

public class PieceTrickCollapseBlock extends PieceTrick {

	SpellParam position;

	public PieceTrickCollapseBlock(Spell spell) {
		super(spell);
	}

	@Override
	public void initParams() {
		addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
	}

	@Override
	public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
		super.addToMetadata(meta);

		meta.addStat(EnumSpellStat.POTENCY, 80);
		meta.addStat(EnumSpellStat.COST, 125);
	}

	@Override
	public Object execute(SpellContext context) throws SpellRuntimeException {
		if(context.caster.worldObj.isRemote)
			return null;

		Vector3 positionVal = this.<Vector3>getParamValue(context, position);

		if(positionVal == null)
			throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
		if(!context.isInRadius(positionVal))
			throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);

		World world = context.caster.worldObj;
		BlockPos pos = new BlockPos(positionVal.x, positionVal.y, positionVal.z);
		BlockPos posDown = pos.down();
		IBlockState state = world.getBlockState(pos);
		IBlockState stateDown = world.getBlockState(posDown);
		Block block = state.getBlock();
		Block blockBelow = stateDown.getBlock();
		
		if(!world.isBlockModifiable(context.caster, pos))
			return null;

		if(blockBelow.isAir(stateDown, world, posDown) && block.getBlockHardness(state, world, pos) != -1 && ForgeHooks.canHarvestBlock(block, context.caster, world, pos) && world.getTileEntity(pos) == null && block.canSilkHarvest(world, pos, state, context.caster)) {
			if(state.getBlock() == Blocks.LIT_REDSTONE_ORE) {
				state = Blocks.REDSTONE_ORE.getDefaultState();
				world.setBlockState(pos, state);
			}
			
			EntityFallingBlock falling = new EntityFallingBlock(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, state);
			world.spawnEntityInWorld(falling);
		}
		return null;
	}

}
