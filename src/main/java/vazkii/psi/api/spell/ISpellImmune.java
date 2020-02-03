/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 * 
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 * 
 * File Created @ [11/03/2016, 19:44:40 (GMT)]
 */
package vazkii.psi.api.spell;

import net.minecraft.entity.Entity;
import vazkii.psi.api.PsiAPI;

/**
 * This interface defines an entity that's immune to spells. Any bosses (IBossDisplayData)
 * will also be immune.
 *
 * If an entity provides a capability of type ISpellImmune,
 * they will also be immune if that returns true.
 */
public interface ISpellImmune {



	boolean isImmune();
	
	static boolean isImmune(Entity e) {
        return !e.isNonBoss() || e.getCapability(PsiAPI.SPELL_IMMUNE_CAPABILITY).map(ISpellImmune::isImmune).orElse(false);
    }
	
}
