package dev.eriksonn.aeronautics.content.particle;

import net.minecraft.core.registries.BuiltInRegistries;
import io.netty.buffer.ByteBuf;
import com.mojang.serialization.Codec;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.StringReader;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;
import dev.eriksonn.aeronautics.index.AeroParticleTypes;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import foundry.veil.backport.network.codec.StreamCodec;

public class AirPoofParticleData implements ParticleOptions, ICustomParticleDataWithSprite<AirPoofParticleData> {
    public static final AirPoofParticleData INSTANCE = new AirPoofParticleData();
    private static final Codec<AirPoofParticleData> CODEC = Codec.unit(INSTANCE);
    private static final StreamCodec<ByteBuf, AirPoofParticleData> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private AirPoofParticleData() {}

    @Override
    public ParticleEngine.SpriteParticleRegistration<AirPoofParticleData> getMetaFactory() {
        return AirPoofParticle.Factory::new;
    }

    @Override
    public Codec<AirPoofParticleData> getCodec(final ParticleType<AirPoofParticleData> type) {
        return CODEC;
    }

    // 1.20.1: particle options are (de)serialized through a Deserializer + writeToNetwork/writeToString instead of a StreamCodec
    public static final ParticleOptions.Deserializer<AirPoofParticleData> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public AirPoofParticleData fromCommand(final ParticleType<AirPoofParticleData> type, final StringReader reader) throws CommandSyntaxException {
            return INSTANCE;
        }

        @Override
        public AirPoofParticleData fromNetwork(final ParticleType<AirPoofParticleData> type, final FriendlyByteBuf buffer) {
            return STREAM_CODEC.decode(buffer);
        }
    };

    @Override
    public ParticleOptions.Deserializer<AirPoofParticleData> getDeserializer() {
        return DESERIALIZER;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buffer) {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public String writeToString() {
        return String.valueOf(BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()));
    }

    @Override
    public ParticleType<?> getType() {
        return AeroParticleTypes.AIR_POOF.get();
    }
}
