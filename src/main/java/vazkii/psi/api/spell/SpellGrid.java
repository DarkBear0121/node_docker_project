/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [16/01/2016, 15:19:16 (GMT)]
 */
package vazkii.psi.api.spell;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Holder class for a spell's piece grid. Pretty much all internal, nothing to see here.
 */
public final class SpellGrid {

	private static final String TAG_SPELL_LIST = "spellList";
	
	private static final String TAG_SPELL_POS_X_LEGACY = "spellPosX";
	private static final String TAG_SPELL_POS_Y_LEGACY = "spellPosY";
	private static final String TAG_SPELL_DATA_LEGACY = "spellData";
	
	private static final String TAG_SPELL_POS_X = "x";
	private static final String TAG_SPELL_POS_Y = "y";
	private static final String TAG_SPELL_DATA = "data";

	public static final int GRID_SIZE = 9;

	public final Spell spell;
	public SpellPiece[][] gridData;

	private boolean empty;
	private int leftmost, rightmost, topmost, bottommost;

	@SideOnly(Side.CLIENT)
	public void draw() {
		for(int i = 0; i < GRID_SIZE; i++)
			for(int j = 0; j < GRID_SIZE; j++) {
				SpellPiece p = gridData[i][j];
				if(p != null) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(i * 18, j * 18, 0);
					p.draw();
					GlStateManager.popMatrix();
				}
			}
	}

	private void recalculateBoundaries() {
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				SpellPiece p = gridData[i][j];
				if (p != null) {
					empty = false;
					if (i < leftmost)
						leftmost = i;
					if (i > rightmost)
						rightmost = i;
					if (j < topmost)
						topmost = j;
					if (j > bottommost)
						bottommost = j;
				}
			}
		}
	}

	public int getSize() {
		recalculateBoundaries();

		if(empty)
			return 0;

		return Math.max(rightmost - leftmost + 1, bottommost - topmost + 1);
	}

	public boolean shift(SpellParam.Side side, boolean doit) {
		recalculateBoundaries();

		if(empty)
			return false;

		if(exists(leftmost + side.offx, topmost + side.offy) && exists(rightmost + side.offx, bottommost + side.offy)) {
			if(!doit)
				return true;

			SpellPiece[][] newGrid = new SpellPiece[GRID_SIZE][GRID_SIZE];

			for(int i = 0; i < GRID_SIZE; i++)
				for(int j = 0; j < GRID_SIZE; j++) {
					SpellPiece p = gridData[i][j];

					if(p != null) {
						int newx = i + side.offx;
						int newy = j + side.offy;
						newGrid[newx][newy] = p;
						p.x = newx;
						p.y = newy;
					}
				}

			gridData = newGrid;
			return true;
		}
		return false;
	}

	public static boolean exists(int x, int y) {
		return x >= 0 && y >= 0 && x < GRID_SIZE && y < GRID_SIZE;
	}

	public SpellPiece getPieceAtSideWithRedirections(int x, int y, SpellParam.Side side) throws SpellCompilationException {
		return getPieceAtSideWithRedirections(new ArrayList<>(), x, y, side);
	}

	public SpellPiece getPieceAtSideWithRedirections(List<SpellPiece> traversed, int x, int y, SpellParam.Side side) throws SpellCompilationException {
		SpellPiece atSide = getPieceAtSideSafely(x, y, side);
		if(traversed.contains(atSide))
			throw new SpellCompilationException(SpellCompilationException.INFINITE_LOOP);

		traversed.add(atSide);
		if(!(atSide instanceof IGenericRedirector))
			return atSide;

		IGenericRedirector redirector = (IGenericRedirector) atSide;
		SpellParam.Side rside = redirector.remapSide(side);
		if(!rside.isEnabled())
			return null;

		return getPieceAtSideWithRedirections(traversed, atSide.x, atSide.y, rside);
	}

	public SpellPiece getPieceAtSideSafely(int x, int y, SpellParam.Side side) {
		int xp = x + side.offx;
		int yp = y + side.offy;
		if(!exists(xp, yp))
			return null;

		return gridData[xp][yp];
	}

	public SpellGrid(Spell spell) {
		this.spell = spell;
		gridData = new SpellPiece[GRID_SIZE][GRID_SIZE];
	}

	public boolean isEmpty() {
		for(int i = 0; i < GRID_SIZE; i++)
			for(int j = 0; j < GRID_SIZE; j++) {
				SpellPiece piece = gridData[i][j];
				if(piece != null)
					return false;
			}

		return true;
	}

	public void readFromNBT(NBTTagCompound cmp) {
		gridData = new SpellPiece[GRID_SIZE][GRID_SIZE];

		NBTTagList list = cmp.getTagList(TAG_SPELL_LIST, 10);
		int len = list.tagCount();
		for(int i = 0; i < len; i++) {
			NBTTagCompound lcmp = list.getCompoundTagAt(i);
			int posX, posY;
			
			if(lcmp.hasKey(TAG_SPELL_POS_X_LEGACY)) {
				posX = lcmp.getInteger(TAG_SPELL_POS_X_LEGACY);
				posY = lcmp.getInteger(TAG_SPELL_POS_Y_LEGACY);
			} else {
				posX = lcmp.getInteger(TAG_SPELL_POS_X);
				posY = lcmp.getInteger(TAG_SPELL_POS_Y);
			}
			
			NBTTagCompound data;
			if(lcmp.hasKey(TAG_SPELL_DATA_LEGACY))
				data = lcmp.getCompoundTag(TAG_SPELL_DATA_LEGACY);
			else data = lcmp.getCompoundTag(TAG_SPELL_DATA);
			
			SpellPiece piece = SpellPiece.createFromNBT(spell, data);
			if(piece != null) {
				gridData[posX][posY] = piece;
				piece.isInGrid = true;
				piece.x = posX;
				piece.y = posY;
			}
		}
	}

	public void writeToNBT(NBTTagCompound cmp) {
		NBTTagList list = new NBTTagList();
		for(int i = 0; i < GRID_SIZE; i++)
			for(int j = 0; j < GRID_SIZE; j++) {
				SpellPiece piece = gridData[i][j];
				if(piece != null) {
					NBTTagCompound lcmp = new NBTTagCompound();
					lcmp.setInteger(TAG_SPELL_POS_X, i);
					lcmp.setInteger(TAG_SPELL_POS_Y, j);

					NBTTagCompound data = new NBTTagCompound();
					piece.writeToNBT(data);
					lcmp.setTag(TAG_SPELL_DATA, data);

					list.appendTag(lcmp);
				}
			}

		cmp.setTag(TAG_SPELL_LIST, list);
	}

}

