package dev.simulated_team.simulated.content.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;
import dev.simulated_team.simulated.index.SimParticleTypes;
import io.netty.buffer.ByteBuf;
import foundry.veil.backport.network.codec.VanillaStreamCodecs;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import foundry.veil.backport.network.codec.ByteBufCodecs;
import foundry.veil.backport.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public class MagnetFieldParticleData2 implements ParticleOptions, ICustomParticleDataWithSprite<MagnetFieldParticleData2> {
    //public static final Codec<MagnetFieldParticleData2> CODEC = RecordCodecBuilder.create((i) -> {
    //    return i.group(Codec.BOOL.fieldOf("negative").forGetter((p) -> {
    //        return p.negative;
    //    })).apply(i, MagnetFieldParticleData2::new);
    //});

    public static final Codec<MagnetFieldParticleData2> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.fieldOf("previous_offset").forGetter(p -> p.previousOffset),
            Vec3.CODEC.fieldOf("next_offset").forGetter(p -> p.nextOffset),
            Codec.BOOL.fieldOf("negative").forGetter(p -> p.negative),
            Codec.INT.fieldOf("timeUntilEnd").forGetter(p -> p.timeUntilEnd)
    ).apply(instance, MagnetFieldParticleData2::new));

    public static final StreamCodec<ByteBuf, MagnetFieldParticleData2> STREAM_CODEC;
    Vec3 previousOffset;
    Vec3 nextOffset;
    private boolean negative;
    private int timeUntilEnd;

    public MagnetFieldParticleData2(final Vec3 previousOffset, final Vec3 nextOffset, final boolean negative, final int timeUntilEnd) {
        this.negative = negative;
        this.previousOffset = previousOffset;
        this.nextOffset = nextOffset;
        this.timeUntilEnd = timeUntilEnd;
    }

    public MagnetFieldParticleData2() {
        this.negative = false;
    }

    public ParticleType<?> getType() {
        return SimParticleTypes.MAGNET_FIELD2.get();
    }

    public Codec<MagnetFieldParticleData2> getCodec(final ParticleType<MagnetFieldParticleData2> type) {
        return CODEC;
    }

    public ParticleEngine.SpriteParticleRegistration<MagnetFieldParticleData2> getMetaFactory() {
        return MagnetFieldParticle2.Factory::new;
    }

    static {
        STREAM_CODEC = StreamCodec.composite(
                VanillaStreamCodecs.VEC3, p -> p.previousOffset,
                VanillaStreamCodecs.VEC3, p -> p.nextOffset,
                ByteBufCodecs.BOOL, p -> p.negative,
                ByteBufCodecs.INT,p -> p.timeUntilEnd,
                MagnetFieldParticleData2::new);
    }

    public boolean isNegative() {
        return this.negative;
    }

    public int getTimeUntilEnd()
    {
        return this.timeUntilEnd;
    }

    public void setNegative(final boolean negative) {
        this.negative = negative;
    }

    // 1.20.1: particle options are (de)serialized through a Deserializer + writeToNetwork/writeToString instead of a StreamCodec
    public static final ParticleOptions.Deserializer<MagnetFieldParticleData2> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public MagnetFieldParticleData2 fromCommand(final ParticleType<MagnetFieldParticleData2> type, final StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            final Vec3 previousOffset = new Vec3(reader.readDouble(), readSpaced(reader), readSpaced(reader));
            reader.expect(' ');
            final Vec3 nextOffset = new Vec3(reader.readDouble(), readSpaced(reader), readSpaced(reader));
            reader.expect(' ');
            final boolean negative = reader.readBoolean();
            reader.expect(' ');
            return new MagnetFieldParticleData2(previousOffset, nextOffset, negative, reader.readInt());
        }

        @Override
        public MagnetFieldParticleData2 fromNetwork(final ParticleType<MagnetFieldParticleData2> type, final FriendlyByteBuf buffer) {
            return STREAM_CODEC.decode(buffer);
        }
    };

    private static double readSpaced(final StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        return reader.readDouble();
    }

    @Override
    public ParticleOptions.Deserializer<MagnetFieldParticleData2> getDeserializer() {
        return DESERIALIZER;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buffer) {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %f %f %f %f %f %f %b %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.previousOffset.x, this.previousOffset.y, this.previousOffset.z, this.nextOffset.x, this.nextOffset.y, this.nextOffset.z, this.negative, this.timeUntilEnd);
    }
}
