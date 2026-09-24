package dev.simulated_team.simulated.gametest;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterEntries;
import foundry.veil.backport.network.RegistryFriendlyByteBuf;
import foundry.veil.backport.network.codec.StreamCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceKey;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import net.createmod.catnip.data.Pair;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.joml.Quaterniond;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Round-trips every static {@link StreamCodec} field declared by Simulated, Aeronautics and Offroad whose value type is a
 * record: encode a sample value, decode it, encode again, and require identical bytes.
 * <p>
 * Singleplayer never serializes custom payloads (the integrated server passes packet objects in memory), so a codec
 * that doesn't round-trip only breaks on dedicated servers. This catches that without one.
 */
@GameTestHolder(Simulated.MOD_ID)
@PrefixGameTestTemplate(false) // reuses an existing structure; the test doesn't need blocks
public class StreamCodecRoundTripTest {
    private static final String[] PACKAGES = {"dev.simulated_team.", "dev.eriksonn.aeronautics.", "dev.ryanhcode.offroad."};

    @GameTest(template = "extrakineticstest.swivelbearing")
    public static void allCodecsRoundTrip(final GameTestHelper helper) {
        final RegistryAccess registries = helper.getLevel().registryAccess();
        final List<String> failures = new ArrayList<>();
        final List<String> skipped = new ArrayList<>();
        int checked = 0;

        for (final Class<?> owner : ourClasses()) {
            final Field[] fields;
            try {
                fields = owner.getDeclaredFields();
            } catch (final Throwable t) {
                continue;
            }

            for (final Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers()) || !StreamCodec.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                final String name = owner.getName() + "#" + field.getName();
                final Type valueGeneric = valueGenericType(field.getGenericType());
                final Class<?> valueType = valueGeneric == null ? null : raw(valueGeneric);
                if (valueType == null) {
                    skipped.add(name + " (" + field.getGenericType().getTypeName() + ")");
                    continue;
                }

                try {
                    field.setAccessible(true);
                    @SuppressWarnings("unchecked") final StreamCodec<ByteBuf, Object> codec = (StreamCodec<ByteBuf, Object>) field.get(null);
                    final Object value = sample(valueGeneric, valueType, 0);

                    final byte[] first = encode(codec, value, registries);
                    final Object decoded = codec.decode(new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(first), registries));
                    final byte[] second = encode(codec, decoded, registries);

                    if (!java.util.Arrays.equals(first, second)) {
                        failures.add(name + ": bytes differ after a round trip\n    " + ByteBufUtil.hexDump(first) + "\n    " + ByteBufUtil.hexDump(second));
                    }
                    checked++;
                } catch (final UnsupportedOperationException e) {
                    skipped.add(name + " (" + e.getMessage() + ")");
                } catch (final Throwable t) {
                    failures.add(name + ": " + t);
                    Simulated.LOGGER.error("Stream codec {} failed to round-trip", name, t);
                }
            }
        }

        Simulated.LOGGER.info("Stream codec round trip: {} checked, {} skipped, {} failed", checked, skipped.size(), failures.size());
        skipped.forEach(s -> Simulated.LOGGER.info("  skipped {}", s));
        failures.forEach(s -> Simulated.LOGGER.error("  FAILED {}", s));

        if (!failures.isEmpty()) {
            helper.fail(failures.size() + " stream codecs don't round-trip, see the log");
        }
        helper.succeed();
    }

    private static byte[] encode(final StreamCodec<ByteBuf, Object> codec, final Object value, final RegistryAccess registries) {
        final RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        codec.encode(buf, value);
        final byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    private static List<Class<?>> ourClasses() {
        final Set<String> names = new HashSet<>();
        for (final ModFileScanData scan : ModList.get().getAllScanData()) {
            for (final ModFileScanData.ClassData data : scan.getClasses()) {
                final String name = data.clazz().getClassName();
                for (final String prefix : PACKAGES) {
                    if (name.startsWith(prefix) && !name.contains(".mixin.")) {
                        names.add(name);
                    }
                }
            }
        }

        final ClassLoader loader = StreamCodecRoundTripTest.class.getClassLoader();
        final List<Class<?>> classes = new ArrayList<>();
        for (final String name : names) {
            // Only load classes that declare a static codec field. Loading every class would also load the client-only
            // ones, and Forge logs an error for each of those on the GameTest server.
            if (!hasStaticCodecField(loader, name)) {
                continue;
            }

            try {
                classes.add(Class.forName(name, true, loader));
            } catch (final Throwable ignored) {
                // Client-only classes don't load on the GameTest server
            }
        }
        return classes;
    }

    private static boolean hasStaticCodecField(final ClassLoader loader, final String name) {
        try (final InputStream stream = loader.getResourceAsStream(name.replace('.', '/') + ".class")) {
            if (stream == null) {
                return true;
            }

            final boolean[] found = {false};
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public FieldVisitor visitField(final int access, final String fieldName, final String descriptor, final String signature, final Object value) {
                    if ((access & Opcodes.ACC_STATIC) != 0 && descriptor.endsWith("Codec;")) {
                        found[0] = true;
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return found[0];
        } catch (final IOException e) {
            return true;
        }
    }

    private static Type valueGenericType(final Type type) {
        if (type instanceof final ParameterizedType parameterized && parameterized.getActualTypeArguments().length == 2) {
            return parameterized.getActualTypeArguments()[1];
        }
        return null;
    }

    private static Class<?> raw(final Type type) {
        if (type instanceof final Class<?> c) return c;
        if (type instanceof final ParameterizedType p) return (Class<?>) p.getRawType();
        return null;
    }

    private static final Map<Class<?>, Object> SAMPLES = new HashMap<>();

    static {
        SAMPLES.put(boolean.class, true);
        SAMPLES.put(Boolean.class, true);
        SAMPLES.put(byte.class, (byte) 3);
        SAMPLES.put(Byte.class, (byte) 3);
        SAMPLES.put(short.class, (short) 7);
        SAMPLES.put(Short.class, (short) 7);
        SAMPLES.put(int.class, 42);
        SAMPLES.put(Integer.class, 42);
        SAMPLES.put(long.class, 1234567L);
        SAMPLES.put(Long.class, 1234567L);
        SAMPLES.put(float.class, 1.5f);
        SAMPLES.put(Float.class, 1.5f);
        SAMPLES.put(double.class, 2.25);
        SAMPLES.put(Double.class, 2.25);
        SAMPLES.put(String.class, "sample");
        SAMPLES.put(UUID.class, UUID.fromString("0a1b2c3d-0000-4000-8000-00000000abcd"));
        SAMPLES.put(BlockPos.class, new BlockPos(1, 2, 3));
        SAMPLES.put(ChunkPos.class, new ChunkPos(4, 5));
        SAMPLES.put(Vec3.class, new Vec3(1.5, 2.5, 3.5));
        SAMPLES.put(Vector3d.class, new Vector3d(1.5, 2.5, 3.5));
        SAMPLES.put(Vector3dc.class, new Vector3d(1.5, 2.5, 3.5));
        SAMPLES.put(Vector3f.class, new Vector3f(1.5f, 2.5f, 3.5f));
        SAMPLES.put(Quaterniond.class, new Quaterniond().rotateXYZ(0.1, 0.2, 0.3));
        SAMPLES.put(Quaterniondc.class, new Quaterniond().rotateXYZ(0.1, 0.2, 0.3));
        SAMPLES.put(Quaternionf.class, new Quaternionf().rotateXYZ(0.1f, 0.2f, 0.3f));
        SAMPLES.put(ResourceLocation.class, new ResourceLocation("simulated", "sample"));
        SAMPLES.put(Direction.class, Direction.EAST);
        SAMPLES.put(Component.class, Component.literal("sample"));
        SAMPLES.put(GlobalPos.class, GlobalPos.of(Level.OVERWORLD, new BlockPos(1, 2, 3)));
    }

    private static Object sample(final Type type, final Class<?> raw, final int depth) {
        if (depth > 4) throw new UnsupportedOperationException("nested too deeply");
        if (raw == null) throw new UnsupportedOperationException("no sample for " + type.getTypeName());

        if (SAMPLES.containsKey(raw)) return SAMPLES.get(raw);
        if (raw == ItemStack.class) return new ItemStack(Items.STONE, 2);
        if (raw == CompoundTag.class) {
            final CompoundTag tag = new CompoundTag();
            tag.putInt("sample", 1);
            return tag;
        }
        if (raw.isEnum()) return raw.getEnumConstants()[raw.getEnumConstants().length - 1];
        if (raw == AABB.class) return new AABB(1, 2, 3, 4.5, 5.5, 6.5);
        if (raw == BoundingBox3d.class || raw == BoundingBox3dc.class) return new BoundingBox3d(1, 2, 3, 4.5, 5.5, 6.5);
        // Registry-backed values have to be registered to encode
        if (raw == ForceGroup.class) return ForceGroups.REGISTRY.iterator().next();
        if (raw == LinkedTypewriterEntries.KeyboardEntry.class) {
            return LinkedTypewriterEntries.KeyboardEntry.createFromCodec(new ItemStack(Items.REDSTONE), new ItemStack(Items.STONE), 65);
        }
        if (raw == ResourceKey.class && element(type, 0).getTypeName().equals(Level.class.getName())) return Level.OVERWORLD;
        if (raw == Pair.class) {
            return Pair.of(sample(element(type, 0), raw(element(type, 0)), depth + 1), sample(element(type, 1), raw(element(type, 1)), depth + 1));
        }

        if (raw == Optional.class) return Optional.of(sample(element(type, 0), raw(element(type, 0)), depth + 1));
        if (List.class.isAssignableFrom(raw) || raw == Collection.class || raw == Iterable.class) {
            return new ArrayList<>(List.of(sample(element(type, 0), raw(element(type, 0)), depth + 1)));
        }
        if (Set.class.isAssignableFrom(raw)) {
            return new HashSet<>(Set.of(sample(element(type, 0), raw(element(type, 0)), depth + 1)));
        }
        if (Map.class.isAssignableFrom(raw)) {
            final Map<Object, Object> map = new HashMap<>();
            map.put(sample(element(type, 0), raw(element(type, 0)), depth + 1), sample(element(type, 1), raw(element(type, 1)), depth + 1));
            return map;
        }

        if (raw.isRecord()) {
            final RecordComponent[] components = raw.getRecordComponents();
            final Class<?>[] types = new Class<?>[components.length];
            final Object[] args = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                types[i] = components[i].getType();
                args[i] = sample(components[i].getGenericType(), types[i], depth + 1);
            }
            try {
                final Constructor<?> constructor = raw.getDeclaredConstructor(types);
                constructor.setAccessible(true);
                return constructor.newInstance(args);
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException("Couldn't construct " + raw.getName(), e);
            }
        }

        // Plain classes: the public constructor with the most parameters we can fill
        Constructor<?> best = null;
        Object[] bestArgs = null;
        for (final Constructor<?> constructor : raw.getConstructors()) {
            if (best != null && constructor.getParameterCount() <= best.getParameterCount()) continue;
            try {
                final Type[] params = constructor.getGenericParameterTypes();
                final Object[] args = new Object[params.length];
                for (int i = 0; i < params.length; i++) {
                    args[i] = sample(params[i], raw(params[i]), depth + 1);
                }
                best = constructor;
                bestArgs = args;
            } catch (final UnsupportedOperationException ignored) {
            }
        }
        if (best != null && !Modifier.isAbstract(raw.getModifiers()) && !raw.isInterface()) {
            try {
                return best.newInstance(bestArgs);
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException("Couldn't construct " + raw.getName(), e);
            }
        }

        throw new UnsupportedOperationException("no sample for " + type.getTypeName());
    }

    private static Type element(final Type type, final int index) {
        if (type instanceof final ParameterizedType parameterized) return parameterized.getActualTypeArguments()[index];
        throw new UnsupportedOperationException("raw collection " + type.getTypeName());
    }
}
