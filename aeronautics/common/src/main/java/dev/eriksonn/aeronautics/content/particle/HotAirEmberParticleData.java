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

public class HotAirEmberParticleData implements ParticleOptions, ICustomParticleDataWithSprite<HotAirEmberParticleData> {

    private static final Codec<HotAirEmberParticleData> CODEC = RecordCodecBuilder.create((i) -> i.group(
                    Codec.BOOL.fieldOf("isSoul").forGetter((p) -> p.isSoul)
    ).apply(i, HotAirEmberParticleData::new));

    private static final StreamCodec<ByteBuf, HotAirEmberParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, (p) -> p.isSoul,
            HotAirEmberParticleData::new);

    protected final boolean isSoul;

    public HotAirEmberParticleData(final boolean isSoul) {
        this.isSoul = isSoul;
    }

    public HotAirEmberParticleData() {
        this.isSoul = false;
    }

    @Override
    public ParticleType<?> getType() {
        return AeroParticleTypes.HOT_AIR_EMBER.get();
    }

    @Override
    public ParticleEngine.SpriteParticleRegistration<HotAirEmberParticleData> getMetaFactory() {
        return HotAirEmberParticle.Factory::new;
    }

    @Override
    public Codec<HotAirEmberParticleData> getCodec(final ParticleType<HotAirEmberParticleData> particleType) {
        return CODEC;
    }

    // 1.20.1: particle options are (de)serialized through a Deserializer + writeToNetwork/writeToString instead of a StreamCodec
    public static final ParticleOptions.Deserializer<HotAirEmberParticleData> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public HotAirEmberParticleData fromCommand(final ParticleType<HotAirEmberParticleData> type, final StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            return new HotAirEmberParticleData(reader.readBoolean());
        }

        @Override
        public HotAirEmberParticleData fromNetwork(final ParticleType<HotAirEmberParticleData> type, final FriendlyByteBuf buffer) {
            return STREAM_CODEC.decode(buffer);
        }
    };

    @Override
    public ParticleOptions.Deserializer<HotAirEmberParticleData> getDeserializer() {
        return DESERIALIZER;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buffer) {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public String writeToString() {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + this.isSoul;
    }
}
