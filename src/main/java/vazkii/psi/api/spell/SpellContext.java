/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [16/01/2016, 19:56:25 (GMT)]
 */
package vazkii.psi.api.spell;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.MathHelper;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.CompiledSpell.Action;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Context for a spell. Used for casting it.
 */
public final class SpellContext {

	/**
	 * The maximum distance from the spell's {@link #focalPoint} a piece of the spell can interact with.<br>
	 * This should be checked against in any tricks that affect parts of the world given a position
	 * or entity.
	 * @see #isInRadius(Entity), {@link #isInRadius(Vector3)}, {@link #isInRadius(double, double, double)}
	 */
	public static final double MAX_DISTANCE = 32;

	/**
	 * The player casting this spell.
	 */
	public PlayerEntity caster;

	/**
	 * The focal point of this spell. This can be the same as {@link #caster}, but will often be different,
	 * like in cases where the spell is executed through a projectile bullet.
	 */
	public Entity focalPoint;

	/**
	 * The compiled spell to execute.
	 */
	public CompiledSpell cspell;

	/**
	 * The loopcast index of this context. This is always 0 when the spell is cast as not a
	 * loopcast. Increments every time for each loopcast iteration.
	 */
	public int loopcastIndex = 0;

	/**
	 * Which hand the object containing this spell was cast from.
	 * <p>
	 * This is only used for loopcasting. If the context doesn't support loopcasting,
	 * there is no need to set this field.
	 */
	public Hand castFrom;

	// Tool stuff. Only available if the spell is casted from a Psimetal Tool
	public ItemStack tool = ItemStack.EMPTY;
	public BlockRayTraceResult positionBroken;
	// Sword stuff
	public LivingEntity attackedEntity;
	// Armor Stuff
	public LivingEntity attackingEntity;
	public double damageTaken;

	// Target slot stuff, for building tricks
	public int targetSlot = 1;
	public boolean shiftTargetSlot = true;
	public boolean customTargetSlot = false;
	
	/**
	 * A map for custom data where addon authors can put stuff. If you're going to put
	 * anything here, prefix it with your mod ID to prevent collision. For example, Trick: Add Motion
	 * uses psi:Entity1MotionX.
	 */
	public final Map<String, Object> customData = new HashMap<>();

	// Runtime information, do not mess with =================================================
	public final Object[][] evaluatedObjects = new Object[SpellGrid.GRID_SIZE][SpellGrid.GRID_SIZE];
	public Stack<Action> actions = null;

	public boolean stopped = false;
	public int delay = 0;
	// End Runtime information ===============================================================

	/**
	 * Sets the {@link #caster} and returns itself. This also calls {@link #setFocalPoint(Entity)}.
	 */
	public SpellContext setPlayer(PlayerEntity player) {
		caster = player;
		return setFocalPoint(player);
	}

	/**
	 * Sets the focal point and returns itself.
	 */
	public SpellContext setFocalPoint(Entity e) {
		focalPoint = e;
		return this;
	}

	/**
	 * Set the compiled spell and returns itself. This should only be called
	 * when you already have a compiled spell, for some reason. For any other case,
	 * use {@link #setSpell(Spell)}.
	 */
	public SpellContext setCompiledSpell(CompiledSpell spell) {
		cspell = spell;
		return this;
	}

	/**
	 * Compiles the passed in spell and passes it to {@link #setCompiledSpell(CompiledSpell)}, returns itself.
	 * This will compile a spell or poll the spell cache for it.
	 */
	public SpellContext setSpell(Spell spell) {
		setCompiledSpell(PsiAPI.internalHandler.getSpellCache().getCompiledSpell(spell));
		return this;
	}

	public SpellContext setLoopcastIndex(int i) {
		loopcastIndex = i;
		return this;
	}

	public boolean isValid() {
		return cspell != null;
	}

	public boolean shouldSuppressErrors() {
		return isValid() && cspell.metadata.errorsSuppressed;
	}

	/**
	 * Used to check if a vector is within this context's radius.
	 * @see #MAX_DISTANCE
	 */
	public boolean isInRadius(Vector3 vec) {
		return isInRadius(vec.x, vec.y, vec.z);
	}

	/**
	 * Used to check if an entity is within this context's radius.
	 * @see #MAX_DISTANCE
	 */
	public boolean isInRadius(Entity e) {
        if (e == null)
            return false;
        if (e == focalPoint || e == caster)
            return true;

        return isInRadius(e.getX(), e.getY(), e.getZ());
    }

	/**
	 * Used to check if an (x,y,z) position is within this context's radius.
	 * @see #MAX_DISTANCE
	 */
	public boolean isInRadius(double x, double y, double z) {
        return MathHelper.pointDistanceSpace(x, y, z, focalPoint.getX(), focalPoint.getY(), focalPoint.getZ()) <= MAX_DISTANCE;
    }
	
	public void verifyEntity(Entity e) throws SpellRuntimeException {
		if(e == null)
			throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
		
		if(ISpellImmune.isImmune(e))
			throw new SpellRuntimeException(SpellRuntimeException.IMMUNE_TARGET);
	}
	
	public int getTargetSlot() throws SpellRuntimeException {
		int slot;
		if(customTargetSlot)
			return targetSlot;
		if(shiftTargetSlot) {
			int cadSlot = PsiAPI.getPlayerCADSlot(caster);
			if(cadSlot == -1)
				throw new SpellRuntimeException(SpellRuntimeException.NO_CAD);
			
			slot = (cadSlot + targetSlot) % 9;
		} else slot = (targetSlot - 1) % 9; 
			
		if(slot < 0)
			slot = 10 + slot;
		return slot;
	}

}
