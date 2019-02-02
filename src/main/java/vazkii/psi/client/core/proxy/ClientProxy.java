/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [08/01/2016, 21:23:11 (GMT)]
 */
package vazkii.psi.client.core.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.cad.ICADColorizer;
import vazkii.psi.client.core.handler.HUDHandler;
import vazkii.psi.client.core.handler.KeybindHandler;
import vazkii.psi.client.core.handler.ShaderHandler;
import vazkii.psi.client.fx.FXSparkle;
import vazkii.psi.client.fx.FXWisp;
import vazkii.psi.client.render.entity.RenderSpellCircle;
import vazkii.psi.client.render.tile.RenderTileProgrammer;
import vazkii.psi.common.block.tile.TileProgrammer;
import vazkii.psi.common.core.handler.ConfigHandler;
import vazkii.psi.common.core.handler.PersistencyHandler;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.core.proxy.CommonProxy;
import vazkii.psi.common.entity.EntitySpellCircle;

import java.awt.*;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);

		ShaderHandler.init();
		KeybindHandler.init();
		
//		if(ConfigHandler.versionCheckEnabled)
//			new VersionChecker().init();

		ClientRegistry.bindTileEntitySpecialRenderer(TileProgrammer.class, new RenderTileProgrammer());

		RenderingRegistry.registerEntityRenderingHandler(EntitySpellCircle.class, RenderSpellCircle::new);
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public void onLevelUp(int level) {
		HUDHandler.levelUp(level);
	}

	@Override
	public void savePersistency() {
		PersistencyHandler.save(PlayerDataHandler.get(getClientPlayer()).level);
	}

	@Override
	public Color getCADColor(ItemStack cadStack) {
		ICAD icad = (ICAD) cadStack.getItem();
		return new Color(icad.getSpellColor(cadStack));
	}

	@Override
	public Color getColorizerColor(ItemStack colorizer) {
		ICADColorizer icc = (ICADColorizer) colorizer.getItem();
		return new Color(icc.getColor(colorizer));
	}

	@Override
	public void sparkleFX(World world, double x, double y, double z, float r, float g, float b, float motionx, float motiony, float motionz, float size, int m) {
		if(noParticles(world))
			return;

		FXSparkle sparkle = new FXSparkle(world, x, y, z, size, r, g, b, m);
		sparkle.setSpeed(motionx, motiony, motionz);
		Minecraft.getMinecraft().effectRenderer.addEffect(sparkle);
	}

	@Override
	public void sparkleFX(double x, double y, double z, float r, float g, float b, float size, int m) {
		sparkleFX(x, y, z, r, g, b, size, m);
	}

	private static boolean distanceLimit = true;
	private static boolean depthTest = true;

	@Override
	public void setWispFXDistanceLimit(boolean limit) {
		distanceLimit = limit;
	}

	@Override
	public void setWispFXDepthTest(boolean test) {
		depthTest = test;
	}

	@Override
	public void wispFX(World world, double x, double y, double z, float r, float g, float b, float size, float motionx, float motiony, float motionz, float maxAgeMul) {
		if(noParticles(world))
			return;

		FXWisp wisp = new FXWisp(world, x, y, z, size, r, g, b, distanceLimit, depthTest, maxAgeMul);
		wisp.setSpeed(motionx, motiony, motionz);

		Minecraft.getMinecraft().effectRenderer.addEffect(wisp);
	}

	@Override
	public void wispFX(double x, double y, double z, float r, float g, float b, float size) {
		wispFX(x, y, z, r, g, b, size);
	}

	private boolean noParticles(World world) {
		if (world == null)
			return true;

		if(!world.isRemote)
			return true;

		if(!ConfigHandler.useVanillaParticleLimiter)
			return false;

		float chance = 1F;
		if(Minecraft.getMinecraft().gameSettings.particleSetting == 1)
			chance = 0.6F;
		else if(Minecraft.getMinecraft().gameSettings.particleSetting == 2)
			chance = 0.2F;

		return !(chance == 1F) && !(Math.random() < chance);
	}

	@Override
	public String localize(String key, Object... arguments) {
		return I18n.format(key, arguments);
	}
}
