/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [16/01/2016, 15:19:38 (GMT)]
 */
package vazkii.psi.api.spell;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import vazkii.psi.api.internal.TooltipHelper;

/**
 * Base abstract class for a spell parameter. See implementations
 * in vazkii.psi.api.spell.param.
 */
public abstract class SpellParam {

	// Colors
	// These are modifiable for a reason, but you still shouldn't do it
	public static int RED = 0xD22A2A;
	public static int GREEN = 0x3ED22A;
	public static int BLUE = 0x2A55D2;
	public static int PURPLE = 0x752AD2;
	public static int CYAN = 0x2AD0D2;
	public static int YELLOW = 0xD2CC2A; // For entities
	public static int GRAY = 0x767676; // For connectors
	public static int BROWN = 0x664000; // For suppressors

	public static final String PSI_PREFIX = "psi.spellparam.";

	// Generic names
	public static final String GENERIC_NAME_TARGET = PSI_PREFIX + "target";
	public static final String GENERIC_NAME_NUMBER = PSI_PREFIX + "number";
	public static final String GENERIC_NAME_NUMBER1 = PSI_PREFIX + "number1";
	public static final String GENERIC_NAME_NUMBER2 = PSI_PREFIX + "number2";
	public static final String GENERIC_NAME_NUMBER3 = PSI_PREFIX + "number3";
	public static final String GENERIC_NAME_NUMBER4 = PSI_PREFIX + "number4";
	public static final String GENERIC_NAME_VECTOR1 = PSI_PREFIX + "vector1";
	public static final String GENERIC_NAME_VECTOR2 = PSI_PREFIX + "vector2";
	public static final String GENERIC_NAME_VECTOR3 = PSI_PREFIX + "vector3";
	public static final String GENERIC_NAME_VECTOR4 = PSI_PREFIX + "vector4";
	public static final String GENERIC_NAME_POSITION = PSI_PREFIX + "position";
	public static final String GENERIC_NAME_MIN = PSI_PREFIX + "min";
	public static final String GENERIC_NAME_MAX = PSI_PREFIX + "max";
	public static final String GENERIC_NAME_POWER = PSI_PREFIX + "power";
	public static final String GENERIC_NAME_X = PSI_PREFIX + "x";
	public static final String GENERIC_NAME_Y = PSI_PREFIX + "y";
	public static final String GENERIC_NAME_Z = PSI_PREFIX + "z";
	public static final String GENERIC_NAME_RADIUS = PSI_PREFIX + "radius";
	public static final String GENERIC_NAME_DISTANCE = PSI_PREFIX + "distance";
	public static final String GENERIC_NAME_TIME = PSI_PREFIX + "time";
	public static final String GENERIC_NAME_BASE = PSI_PREFIX + "base";
	public static final String GENERIC_NAME_RAY = PSI_PREFIX + "ray";

	public final String name;
	public final int color;
	public final boolean canDisable;

	public SpellParam(String name, int color, boolean canDisable) {
		this.name = name;
		this.color = color;
		this.canDisable = canDisable;
	}

	/**
	 * Gets the type that this parameter requires. This is evaluated against
	 * {@link SpellPiece#getEvaluationType()}.<br>
	 * If you want any type to be able to be accepted, use {@link Any}.class.<br>
	 * This method is to only be used internally, and as such, is not required to be
	 * implemented fully. For better control, use {@link #canAccept(SpellPiece)} and
	 * override {@link #getRequiredTypeString()} for display.
	 */
	protected abstract Class<?> getRequiredType();

	/**
	 * Gets if this parameter requires a constant ({@link EnumPieceType#CONSTANT}). Similarly to {@link #getRequiredType()} this
	 * is for internal use only.
	 */
	protected boolean requiresConstant() {
		return false;
	}

	/**
	 * Gets the string for display for the required type.
	 */
	public ITextComponent getRequiredTypeString() {
		Class<?> evalType = getRequiredType();
		String evalStr = evalType.getSimpleName();
		ITextComponent s = new TranslationTextComponent("psi.datatype." + evalStr);
		if (requiresConstant())
			s.appendText(" ").appendSibling(new TranslationTextComponent("psimisc.constant"));

		return s;
	}

	/**
	 * Gets if this paramtere can accept the piece passed in. Default implementation
	 * checks against {@link #getRequiredType()} and {@link #requiresConstant()}.
	 */
	public boolean canAccept(SpellPiece piece) {
		return (getRequiredType() == Any.class || getRequiredType().isAssignableFrom(piece.getEvaluationType())) && (!requiresConstant() || piece.getPieceType() == EnumPieceType.CONSTANT);
	}

	/**
	 * Helper Enum for the various sides a parameter can take.
	 */
	public enum Side {
		OFF(0, 0, 238, 0),
		TOP(0, -1, 222, 8),
		BOTTOM(0, 1, 230, 8),
		LEFT(-1, 0, 230, 0),
		RIGHT(1, 0, 222, 0);

		public static final Side[] DIRECTIONS = new Side[] { TOP, BOTTOM, LEFT, RIGHT };

		public final int offx, offy, u, v;

		Side(int offx, int offy, int u, int v) {
			this.offx = offx;
			this.offy = offy;
			this.u = u;
			this.v = v;
		}

		public boolean isEnabled() {
			return this != OFF;
		}

		public int asInt() {
			return ordinal();
		}

		public static Side fromInt(int i) {
			return Side.class.getEnumConstants()[i];
		}

		public Side getOpposite() {
			return mapSides(BOTTOM, TOP, RIGHT, LEFT);
		}

		public Side mirrorVertical() {
			return mapSides(BOTTOM, TOP, LEFT, RIGHT);
		}

		public Side rotateCW() {
			return mapSides(LEFT, RIGHT, BOTTOM, TOP);
		}

		public Side rotateCCW() {
			return mapSides(RIGHT, LEFT, TOP, BOTTOM);
		}

		private Side mapSides(Side whenUp, Side whenDown, Side whenL, Side whenR) {
			switch (this) {
				case TOP:
					return whenUp;
				case BOTTOM:
					return whenDown;
				case LEFT:
					return whenL;
				case RIGHT:
					return whenR;
				default:
					return OFF;
			}
		}
	}

	/**
	 * Empty helper class for use with required types when any type is accepted.
	 */
	public static class Any { }

}
