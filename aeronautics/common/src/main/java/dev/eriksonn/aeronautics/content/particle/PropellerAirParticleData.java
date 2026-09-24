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

public class PropellerAirParticleData implements ParticleOptions, ICustomParticleDataWithSprite<PropellerAirParticleData> {

    private static final Codec<PropellerAirParticleData> CODEC = RecordCodecBuilder.create((i) -> i.group(
                    Codec.BOOL.fieldOf("collision").forGetter((p) -> p.enableCollision),
                    Codec.BOOL.fieldOf("virtual").forGetter(p -> p.isVirtual))
            .apply(i, PropellerAirParticleData::new));

    private static final StreamCodec<ByteBuf, PropellerAirParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, (p) -> p.enableCollision,
            ByteBufCodecs.BOOL, (p) -> p.isVirtual,
            PropellerAirParticleData::new);


    boolean enableCollision;
    boolean isVirtual;

    public PropellerAirParticleData(boolean enableCollision, boolean isVirtual) {
        this.enableCollision = enableCollision;
        this.isVirtual = isVirtual;
    }

    public PropellerAirParticleData() {
        this(true, false);
    }

    @Override
    public ParticleType<?> getType() {
        return AeroParticleTypes.PROPELLER_AIR_FLOW.get();
    }

    @Override
    public ParticleEngine.SpriteParticleRegistration<PropellerAirParticleData> getMetaFactory() {
        return PropellerAirParticle.Factory::new;
    }

    @Override
    public Codec<PropellerAirParticleData> getCodec(ParticleType<PropellerAirParticleData> particleType) {
        return CODEC;
    }

    // 1.20.1: particle options are (de)serialized through a Deserializer + writeToNetwork/writeToString instead of a StreamCodec
    public static final ParticleOptions.Deserializer<PropellerAirParticleData> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public PropellerAirParticleData fromCommand(final ParticleType<PropellerAirParticleData> type, final StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            final boolean enableCollision = reader.readBoolean();
            reader.expect(' ');
            return new PropellerAirParticleData(enableCollision, reader.readBoolean());
        }

        @Override
        public PropellerAirParticleData fromNetwork(final ParticleType<PropellerAirParticleData> type, final FriendlyByteBuf buffer) {
            return STREAM_CODEC.decode(buffer);
        }
    };

    @Override
    public ParticleOptions.Deserializer<PropellerAirParticleData> getDeserializer() {
        return DESERIALIZER;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buffer) {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public String writeToString() {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + this.enableCollision + " " + this.isVirtual;
    }
}
