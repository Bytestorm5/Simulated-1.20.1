package dev.eriksonn.aeronautics.content.particle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import io.netty.buffer.ByteBuf;
import com.mojang.serialization.Codec;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;
import dev.eriksonn.aeronautics.index.AeroParticleTypes;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import foundry.veil.backport.network.codec.ByteBufCodecs;
import foundry.veil.backport.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.joml.Quaternionf;

public record GustParticleData(
        Quaternionf orientation) implements ParticleOptions, ICustomParticleDataWithSprite<GustParticleData> {

    private static final Codec<GustParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.QUATERNIONF.fieldOf("orientation").forGetter(o -> o.orientation)
    ).apply(instance, GustParticleData::new));

    private static final StreamCodec<ByteBuf, GustParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.QUATERNIONF, (o -> o.orientation),
            GustParticleData::new
    );

    public GustParticleData() {
        this(new Quaternionf());
    }


    @Override
    public ParticleEngine.SpriteParticleRegistration<GustParticleData> getMetaFactory() {
        return GustParticle.Factory::new;
    }

    @Override
    public Codec<GustParticleData> getCodec(final ParticleType<GustParticleData> type) {
        return CODEC;
    }

    // 1.20.1: particle options are (de)serialized through a Deserializer + writeToNetwork/writeToString instead of a StreamCodec
    public static final ParticleOptions.Deserializer<GustParticleData> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public GustParticleData fromCommand(final ParticleType<GustParticleData> type, final StringReader reader) throws CommandSyntaxException {
            final float[] values = new float[4];
            for (int i = 0; i < values.length; i++) {
                reader.expect(' ');
                values[i] = reader.readFloat();
            }
            return new GustParticleData(new Quaternionf(values[0], values[1], values[2], values[3]));
        }

        @Override
        public GustParticleData fromNetwork(final ParticleType<GustParticleData> type, final FriendlyByteBuf buffer) {
            return STREAM_CODEC.decode(buffer);
        }
    };

    @Override
    public ParticleOptions.Deserializer<GustParticleData> getDeserializer() {
        return DESERIALIZER;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buffer) {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public String writeToString() {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + this.orientation.x() + " " + this.orientation.y() + " " + this.orientation.z() + " " + this.orientation.w();
    }

    @Override
    public ParticleType<?> getType() {
        return AeroParticleTypes.GUST.get();
    }
}
