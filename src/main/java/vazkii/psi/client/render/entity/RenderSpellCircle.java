/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

import org.lwjgl.opengl.GL11;

import vazkii.psi.api.internal.PsiRenderHelper;
import vazkii.psi.common.Psi;
import vazkii.psi.common.entity.EntitySpellCircle;
import vazkii.psi.common.lib.LibMisc;
import vazkii.psi.common.lib.LibResources;

public class RenderSpellCircle extends EntityRenderer<EntitySpellCircle> {

	private static final RenderType[] LAYERS = new RenderType[3];
	static {
		for (int i = 0; i < LAYERS.length; i++) {
			ResourceLocation texture = new ResourceLocation(String.format(LibResources.MISC_SPELL_CIRCLE, i));
			RenderType.State glState = RenderType.State.getBuilder().texture(new RenderState.TextureState(texture, false, false))
					.cull(new RenderState.CullState(false))
					.alpha(new RenderState.AlphaState(0.004F))
					.lightmap(new RenderState.LightmapState(true))
					.build(true);
			LAYERS[i] = RenderType.makeType(LibMisc.MOD_ID + ":spell_circle_" + i, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 64, false, false, glState);
		}
	}

	private static final float BRIGHTNESS_FACTOR = 0.7F;

	public RenderSpellCircle(EntityRendererManager renderManager) {
		super(renderManager);
	}

	@Override
	public void render(EntitySpellCircle entity, float entityYaw, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers, int light) {
		ms.push();
		ItemStack colorizer = entity.getDataManager().get(EntitySpellCircle.COLORIZER_DATA);
		int color = Psi.proxy.getColorForColorizer(colorizer);
		float alive = entity.getTimeAlive() + partialTicks;
		float scale = Math.min(1F, alive / EntitySpellCircle.CAST_DELAY);
		if (alive > EntitySpellCircle.LIVE_TIME - EntitySpellCircle.CAST_DELAY) {
			scale = 1F - Math.min(1F, Math.max(0, alive - (EntitySpellCircle.LIVE_TIME - EntitySpellCircle.CAST_DELAY)) / EntitySpellCircle.CAST_DELAY);
		}
		renderSpellCircle(alive, scale, 1, 0, 1, 0, color, ms, buffers);
		ms.pop();
	}

	public static void renderSpellCircle(float alive, float scale, float horizontalScale, float xDir, float yDir, float zDir, int color, MatrixStack ms, IRenderTypeBuffer buffers) {

		ms.push();
		double ratio = 0.0625 * horizontalScale;

		float mag = xDir * xDir + yDir * yDir + zDir * zDir;
		zDir /= mag;

		if (zDir == -1) {
			ms.rotate(Vector3f.XP.rotationDegrees(180));
		} else if (zDir != 1) {
			ms.rotate(new Vector3f(-yDir / mag, xDir / mag, 0).rotationDegrees((float) (Math.acos(zDir) * 180 / Math.PI)));
		}
		ms.translate(0, 0, 0.1);
		ms.scale((float) ratio * scale, (float) ratio * scale, (float) ratio);

		int r = PsiRenderHelper.r(color);
		int g = PsiRenderHelper.g(color);
		int b = PsiRenderHelper.b(color);

		for (int i = 0; i < LAYERS.length; i++) {
			int rValue = r;
			int gValue = g;
			int bValue = b;

			if (i == 1) {
				rValue = gValue = bValue = 0xFF;
			} else if (i == 2) {
				int minBrightness = (int) (1 / (1 - BRIGHTNESS_FACTOR));
				if (rValue == 0 && gValue == 0 && bValue == 0) {
					rValue = gValue = bValue = minBrightness;
				}
				if (rValue > 0 && rValue < minBrightness) {
					rValue = minBrightness;
				}
				if (gValue > 0 && gValue < minBrightness) {
					gValue = minBrightness;
				}
				if (bValue > 0 && bValue < minBrightness) {
					bValue = minBrightness;
				}

				rValue = (int) Math.min(rValue / BRIGHTNESS_FACTOR, 0xFF);
				gValue = (int) Math.min(gValue / BRIGHTNESS_FACTOR, 0xFF);
				bValue = (int) Math.min(bValue / BRIGHTNESS_FACTOR, 0xFF);
			}

			ms.push();
			ms.rotate(Vector3f.ZP.rotationDegrees(i == 0 ? -alive : alive));

			IVertexBuilder buffer = buffers.getBuffer(LAYERS[i]);
			Matrix4f mat = ms.getLast().getMatrix();
			int fullbright = 0xF000F0;
			buffer.pos(mat, -32, 32, 0).color(rValue, gValue, bValue, 255).tex(0, 1).lightmap(fullbright).endVertex();
			buffer.pos(mat, 32, 32, 0).color(rValue, gValue, bValue, 255).tex(1, 1).lightmap(fullbright).endVertex();
			buffer.pos(mat, 32, -32, 0).color(rValue, gValue, bValue, 255).tex(1, 0).lightmap(fullbright).endVertex();
			buffer.pos(mat, -32, -32, 0).color(rValue, gValue, bValue, 255).tex(0, 0).lightmap(fullbright).endVertex();
			ms.pop();

			ms.translate(0, 0, -0.5);
		}

		ms.pop();
	}

	@Override
	public ResourceLocation getEntityTexture(EntitySpellCircle entitySpellCircle) {
		return null;
	}
}
