/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [24/01/2016, 17:14:15 (GMT)]
 */
package vazkii.psi.common.spell.trick.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
import vazkii.psi.client.core.handler.HUDHandler;
import vazkii.psi.common.block.base.ModBlocks;

public class PieceTrickPlaceBlock extends PieceTrick {

	SpellParam position;

	public PieceTrickPlaceBlock(Spell spell) {
		super(spell);
	}

	@Override
	public void initParams() {
		addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
	}

	@Override
	public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
		super.addToMetadata(meta);

		meta.addStat(EnumSpellStat.POTENCY, 8);
		meta.addStat(EnumSpellStat.COST, 8);
	}

	@Override
	public Object execute(SpellContext context) throws SpellRuntimeException {
		Vector3 positionVal = this.<Vector3>getParamValue(context, position);

		if(positionVal == null)
			throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
		if(!context.isInRadius(positionVal))
			throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);

		BlockPos pos = new BlockPos(positionVal.x, positionVal.y, positionVal.z);
		placeBlock(context.caster, context.caster.worldObj, pos, context.getTargetSlot(), false);

		return null;
	}

	public static void placeBlock(EntityPlayer player, World world, BlockPos pos, int slot, boolean particles) {
		placeBlock(player, world, pos, slot, particles, false);
	}

	public static void placeBlock(EntityPlayer player, World world, BlockPos pos, int slot, boolean particles, boolean conjure) {
		if(!world.isBlockLoaded(pos) || !world.isBlockModifiable(player, pos))
			return;
		
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if(block == null || block.isAir(state, world, pos) || block.isReplaceable(world, pos)) {
			if(conjure) {
				if(!world.isRemote)
					world.setBlockState(pos, ModBlocks.conjured.getDefaultState());
			} else {
				ItemStack stack = player.inventory.getStackInSlot(slot);
				if(stack != null && stack.getItem() instanceof ItemBlock) {
					ItemStack rem = removeFromInventory(player, block, stack);
					ItemBlock iblock = (ItemBlock) stack.getItem();
					
					Block blockToPlace = Block.getBlockFromItem(rem.getItem()); 
					if(!world.isRemote) {
						IBlockState newState = blockToPlace.onBlockPlaced(world, pos, EnumFacing.UP, 0, 0, 0, rem.getItemDamage(), player);
						iblock.placeBlockAt(stack, player, world, pos, EnumFacing.UP, 0, 0, 0, newState);
					}

					if(player.capabilities.isCreativeMode)
						HUDHandler.setRemaining(rem, -1);
					else HUDHandler.setRemaining(player, rem, null);
				}
			}

			if(particles && !world.isRemote)
				world.playEvent(2001, pos, Block.getStateId(world.getBlockState(pos)));
		}
	}

	public static ItemStack removeFromInventory(EntityPlayer player, Block block, ItemStack stack) {
		if(player.capabilities.isCreativeMode)
			return stack.copy();

		InventoryPlayer inv = player.inventory;
		for(int i = inv.getSizeInventory() - 1; i >= 0; i--) {
			ItemStack invStack = inv.getStackInSlot(i);
			if(invStack != null && invStack.isItemEqual(stack)) {
				ItemStack retStack = invStack.copy();
				invStack.stackSize--;
				if(invStack.stackSize == 0)
					inv.setInventorySlotContents(i, null);
				return retStack;
			}
		}

		return null;
	}

}
