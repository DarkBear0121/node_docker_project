package vazkii.psi.client.fx;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.particles.ParticleType;
import net.minecraft.world.World;

//https://github.com/Vazkii/Botania/blob/1.15/src/main/java/vazkii/botania/client/fx/SparkleParticleType.java
public class SparkleParticleType extends ParticleType<SparkleParticleData> {
    public SparkleParticleType() {
        super(false, SparkleParticleData.DESERIALIZER);
    }

    public static class Factory implements IParticleFactory<SparkleParticleData> {
        private final IAnimatedSprite sprite;

        public Factory(IAnimatedSprite sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle makeParticle(SparkleParticleData data, World world, double x, double y, double z, double mx, double my, double mz) {
            return new FXSparkle(world, x, y, z, data.size, data.r, data.g, data.b, data.m, mx, my, mz, sprite);
        }
    }
}
