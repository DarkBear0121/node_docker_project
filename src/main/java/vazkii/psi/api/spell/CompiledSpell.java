/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [16/01/2016, 15:17:40 (GMT)]
 */
package vazkii.psi.api.spell;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.IPlayerData;
import vazkii.psi.api.spell.SpellPiece.Null;
import vazkii.psi.common.core.handler.CrashReportHandler;

import java.util.*;

/**
 * A spell that has been compiled by a compiler and is ready to be executed.
 */
public class CompiledSpell {

	public final Spell sourceSpell;
	public final SpellMetadata metadata = new SpellMetadata();

	public final Stack<Action> actions = new Stack<>();
	public final Map<SpellPiece, CatchHandler> errorHandlers = new HashMap<>();
	public final Map<SpellPiece, Action> actionMap = new HashMap<>();

	public Action currentAction;
	public final boolean[][] spotsEvaluated;

	public CompiledSpell(Spell source) {
		sourceSpell = source;
		metadata.setStat(EnumSpellStat.BANDWIDTH, source.grid.getSize());

		spotsEvaluated = new boolean[SpellGrid.GRID_SIZE][SpellGrid.GRID_SIZE];
	}

	/**
	 * Executes the spell, making a copy of the {@link #actions} stack so it can
	 * be reused if cached.
	 */
	public boolean execute(SpellContext context) throws SpellRuntimeException {
		IPlayerData data = PsiAPI.internalHandler.getDataForPlayer(context.caster);
		while(!context.actions.isEmpty()) {
			Action a = context.actions.pop();
			currentAction = a;

			CrashReportHandler.setSpell(this);
			a.execute(data, context);
			CrashReportHandler.setSpell(null);

			currentAction = null;

			if(context.stopped)
				return false;

			if(context.delay > 0)
				return true;
		}
		
		return false;
	}

	/**
	 * @see #execute
	 */
	@SuppressWarnings("unchecked")
	public void safeExecute(SpellContext context) {
		if (context.caster.getEntityWorld().isRemote)
			return;

		try {
			if(context.actions == null)
				context.actions = (Stack<Action>) actions.clone();

			if(context.cspell.execute(context))
				PsiAPI.internalHandler.delayContext(context);
		} catch(SpellRuntimeException e) {
			if(!context.shouldSuppressErrors()) {
				context.caster.sendMessage(new TextComponentTranslation(e.getMessage()).setStyle(new Style().setColor(TextFormatting.RED)));
				
				int x = context.cspell.currentAction.piece.x + 1;
				int y = context.cspell.currentAction.piece.y + 1;
				context.caster.sendMessage(new TextComponentTranslation("psi.spellerror.position", x, y).setStyle(new Style().setColor(TextFormatting.RED)));
			}
		}
	}

	public boolean hasEvaluated(int x, int y) {
		if(!SpellGrid.exists(x, y))
			return false;

		return spotsEvaluated[x][y];
	}

	public class Action {

		public final SpellPiece piece;

		public Action(SpellPiece piece) {
			this.piece = piece;
		}

		public void execute(IPlayerData data, SpellContext context) throws SpellRuntimeException {
			try {
				data.markPieceExecuted(piece);
				Object o = piece.execute(context);

				Class<?> eval = piece.getEvaluationType();
				if (eval != null && eval != Null.class)
					context.evaluatedObjects[piece.x][piece.y] = o;
			} catch (SpellRuntimeException exception) {
				if (errorHandlers.containsKey(piece)) {
					if (!errorHandlers.get(piece).suppress(piece, context, exception))
						throw exception;
				}
			}
		}

	}

	public class CatchHandler {

		public final SpellPiece handlerPiece;
		public final IErrorCatcher handler;

		public CatchHandler(SpellPiece handlerPiece) {
			this.handlerPiece = handlerPiece;
			this.handler = (IErrorCatcher) handlerPiece;
		}

		public boolean suppress(SpellPiece piece, SpellContext context, SpellRuntimeException exception) {
			boolean handled = handler.catchException(piece, context, exception);
			if (handled) {
				Class<?> eval = piece.getEvaluationType();
				if (eval != null && eval != Null.class)
					context.evaluatedObjects[piece.x][piece.y] =
							handler.supplyReplacementValue(piece, context, exception);
			}

			return handled;
		}
	}

}
