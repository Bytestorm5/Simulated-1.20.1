package dev.eriksonn.aeronautics.content.particle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import io.netty.buffer.ByteBuf;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;
import dev.eriksonn.aeronautics.index.AeroParticleTypes;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import foundry.veil.backport.network.codec.ByteBufCodecs;
import foundry.veil.backport.network.codec.StreamCodec;

public class LevititeSparkleParticleData implements ParticleOptions, ICustomParticleDataWithSprite<LevititeSparkleParticleData> {
    public static final int LEVITITE_GREEN = 9424022;
    public static final int LEVITITE_PINK = 15521489;

    public static final Codec<LevititeSparkleParticleData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("color").forGetter(p -> p.color)
            ).apply(instance, LevititeSparkleParticleData::new));

    public static final StreamCodec<ByteBuf, LevititeSparkleParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, p -> p.color,
            LevititeSparkleParticleData::new
    );

    public final int color;

    public LevititeSparkleParticleData(final int color) {
        this.color = color;
    }

    public LevititeSparkleParticleData() {
        this(LEVITITE_GREEN);
    }

    @Override
    public ParticleEngine.SpriteParticleRegistration<LevititeSparkleParticleData> getMetaFactory() {
        return LevititeSparkleParticle.Factory::new;
    }

    @Override
    public Codec<LevititeSparkleParticleData> getCodec(final ParticleType<LevititeSparkleParticleData> type) {
        return CODEC;
    }

    // 1.20.1: particle options are (de)serialized through a Deserializer + writeToNetwork/writeToString instead of a StreamCodec
    public static final ParticleOptions.Deserializer<LevititeSparkleParticleData> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public LevititeSparkleParticleData fromCommand(final ParticleType<LevititeSparkleParticleData> type, final StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            return new LevititeSparkleParticleData(reader.readInt());
        }

        @Override
        public LevititeSparkleParticleData fromNetwork(final ParticleType<LevititeSparkleParticleData> type, final FriendlyByteBuf buffer) {
            return STREAM_CODEC.decode(buffer);
        }
    };

    @Override
    public ParticleOptions.Deserializer<LevititeSparkleParticleData> getDeserializer() {
        return DESERIALIZER;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buffer) {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public String writeToString() {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + this.color;
    }

    @Override
    public ParticleType<?> getType() {
        return AeroParticleTypes.LEVITITE_SPARKLE.get();
    }
}
