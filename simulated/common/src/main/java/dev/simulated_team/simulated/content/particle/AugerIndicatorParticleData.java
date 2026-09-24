package dev.simulated_team.simulated.content.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;
import dev.simulated_team.simulated.index.SimParticleTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import foundry.veil.backport.network.codec.ByteBufCodecs;
import foundry.veil.backport.network.codec.StreamCodec;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Locale;

/**
 * Stolen shamelessly from {@link com.simibubi.create.content.kinetics.base.RotationIndicatorParticleData}
 */
public class AugerIndicatorParticleData implements ParticleOptions, ICustomParticleDataWithSprite<AugerIndicatorParticleData> {
    public static final Codec<AugerIndicatorParticleData> CODEC = RecordCodecBuilder.create(i -> i
            .group(Codec.INT.fieldOf("color")
                            .forGetter(p -> p.color),
                    Codec.FLOAT.fieldOf("speed")
                            .forGetter(p -> p.speed),
                    Codec.FLOAT.fieldOf("radius1")
                            .forGetter(p -> p.radius1),
                    Codec.FLOAT.fieldOf("radius2")
                            .forGetter(p -> p.radius2),
                    Codec.FLOAT.fieldOf("angle_offset")
                            .forGetter(p -> p.angleOffset),
                    Codec.INT.fieldOf("life_span")
                            .forGetter(p -> p.lifeSpan),
                    Direction.CODEC.fieldOf("direction")
                            .forGetter(p -> p.direction))
            .apply(i, AugerIndicatorParticleData::new));

    // lazy way but its past the maximum number of fields for the composite constructor and i cant be bothered figuring out how to solve it
    public static final StreamCodec<ByteBuf, AugerIndicatorParticleData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public final int color;
    public final float speed;
    public final float radius1;
    public final float radius2;
    public float angleOffset;
    public final int lifeSpan;
    public final Direction direction;

    public AugerIndicatorParticleData(final int color, final float speed,
                                         final float radius1, final float radius2, final float angleOffset,
                                         final int lifeSpan,
                                         final Direction direction) {
        this.color = color;
        this.speed = speed;
        this.radius1 = radius1;
        this.radius2 = radius2;
        this.angleOffset = angleOffset;
        this.lifeSpan = lifeSpan;
        this.direction = direction;
    }

    public AugerIndicatorParticleData() {
        this(0, 0, 0, 0, 0, 0, Direction.NORTH);
    }

    @Override
    public ParticleType<?> getType() {
        return SimParticleTypes.AUGER_INDICATOR.get();
    }

    @Override
    public Codec<AugerIndicatorParticleData> getCodec(final ParticleType<AugerIndicatorParticleData> type) {
        return CODEC;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ParticleEngine.SpriteParticleRegistration<AugerIndicatorParticleData> getMetaFactory() {
        return AugerIndicatorParticle.Factory::new;
    }

    // 1.20.1: particle options are (de)serialized through a Deserializer + writeToNetwork/writeToString instead of a StreamCodec
    public static final ParticleOptions.Deserializer<AugerIndicatorParticleData> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public AugerIndicatorParticleData fromCommand(final ParticleType<AugerIndicatorParticleData> type, final StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            final int color = reader.readInt();
            reader.expect(' ');
            final float speed = reader.readFloat();
            reader.expect(' ');
            final float radius1 = reader.readFloat();
            reader.expect(' ');
            final float radius2 = reader.readFloat();
            reader.expect(' ');
            final float angleOffset = reader.readFloat();
            reader.expect(' ');
            final int lifeSpan = reader.readInt();
            reader.expect(' ');
            final Direction direction = Direction.byName(reader.readUnquotedString());
            return new AugerIndicatorParticleData(color, speed, radius1, radius2, angleOffset, lifeSpan, direction == null ? Direction.NORTH : direction);
        }

        @Override
        public AugerIndicatorParticleData fromNetwork(final ParticleType<AugerIndicatorParticleData> type, final FriendlyByteBuf buffer) {
            return STREAM_CODEC.decode(buffer);
        }
    };

    @Override
    public ParticleOptions.Deserializer<AugerIndicatorParticleData> getDeserializer() {
        return DESERIALIZER;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buffer) {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %d %f %f %f %f %d %s", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.color, this.speed, this.radius1, this.radius2, this.angleOffset, this.lifeSpan, this.direction.getName());
    }
}
