package dev.simulated_team.simulated.content.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;
import dev.simulated_team.simulated.index.SimParticleTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import foundry.veil.backport.network.codec.ByteBufCodecs;
import foundry.veil.backport.network.codec.StreamCodec;

public class MagnetFieldParticleData implements ParticleOptions, ICustomParticleDataWithSprite<MagnetFieldParticleData> {
    public static final Codec<MagnetFieldParticleData> CODEC = RecordCodecBuilder.create((i) -> {
        return i.group(Codec.BOOL.fieldOf("negative").forGetter((p) -> {
            return p.negative;
        })).apply(i, MagnetFieldParticleData::new);
    });
    public static final StreamCodec<ByteBuf, MagnetFieldParticleData> STREAM_CODEC;
    private boolean negative;

    public MagnetFieldParticleData(final boolean negative) {
        this.negative = negative;
    }

    public MagnetFieldParticleData() {
        this.negative = false;
    }

    public ParticleType<?> getType() {
        return SimParticleTypes.MAGNET_FIELD.get();
    }

    public Codec<MagnetFieldParticleData> getCodec(final ParticleType<MagnetFieldParticleData> type) {
        return CODEC;
    }

    public ParticleEngine.SpriteParticleRegistration<MagnetFieldParticleData> getMetaFactory() {
        return MagnetFieldParticle.Factory::new;
    }

    static {
        STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, (p) -> p.negative, MagnetFieldParticleData::new);
    }

    public boolean isNegative() {
        return this.negative;
    }

    public void setNegative(final boolean negative) {
        this.negative = negative;
    }

    // 1.20.1: particle options are (de)serialized through a Deserializer + writeToNetwork/writeToString instead of a StreamCodec
    public static final ParticleOptions.Deserializer<MagnetFieldParticleData> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public MagnetFieldParticleData fromCommand(final ParticleType<MagnetFieldParticleData> type, final StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            return new MagnetFieldParticleData(reader.readBoolean());
        }

        @Override
        public MagnetFieldParticleData fromNetwork(final ParticleType<MagnetFieldParticleData> type, final FriendlyByteBuf buffer) {
            return STREAM_CODEC.decode(buffer);
        }
    };

    @Override
    public ParticleOptions.Deserializer<MagnetFieldParticleData> getDeserializer() {
        return DESERIALIZER;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buffer) {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public String writeToString() {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + this.negative;
    }
}
