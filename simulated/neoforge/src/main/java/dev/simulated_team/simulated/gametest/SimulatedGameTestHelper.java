package dev.simulated_team.simulated.gametest;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraKinetics;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

public class SimulatedGameTestHelper {

    /**
     * 1.20.1: {@link GameTestHelper#getBlockEntity(BlockPos)} isn't generic
     */
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> T getBlockEntity(final GameTestHelper helper, final BlockPos pos) {
        return (T) helper.getBlockEntity(pos);
    }

    /**
     * 1.20.1: {@link GameTestHelper} has no {@code assertBlockEntityData}
     */
    public static <T extends BlockEntity> void assertBlockEntityData(final GameTestHelper helper, final BlockPos pos, final Predicate<T> predicate, final Supplier<String> exceptionMessage) {
        final T be = getBlockEntity(helper, pos);
        if (be == null || !predicate.test(be)) {
            throw new GameTestAssertPosException(exceptionMessage.get(), helper.absolutePos(pos), pos, helper.getTick());
        }
    }

    /**
     * 1.20.1: {@code GameTestHelper#getBounds} is private, so the absolute structure bounds are rebuilt from the
     * structure's blocks
     */
    public static AABB getBounds(final GameTestHelper helper) {
        final BlockPos.MutableBlockPos min = new BlockPos.MutableBlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        final BlockPos.MutableBlockPos max = new BlockPos.MutableBlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        // forEveryBlockInStructure offsets the structure one block up
        helper.forEveryBlockInStructure(pos -> {
            final BlockPos absolute = helper.absolutePos(pos.below());
            min.set(Math.min(min.getX(), absolute.getX()), Math.min(min.getY(), absolute.getY()), Math.min(min.getZ(), absolute.getZ()));
            max.set(Math.max(max.getX(), absolute.getX()), Math.max(max.getY(), absolute.getY()), Math.max(max.getZ(), absolute.getZ()));
        });
        return new AABB(min).minmax(new AABB(max));
    }

    public static <T extends BlockEntity & ExtraKinetics> void assertExtraKinetics(final GameTestHelper helper, final BlockPos pos, final BiPredicate<T, KineticBlockEntity> predicate, final BiFunction<T, KineticBlockEntity, String> exceptionMessage) {
        final T t = getBlockEntity(helper, pos);
        if (!predicate.test(t, t.getExtraKinetics())) {
            throw new GameTestAssertPosException(exceptionMessage.apply(t, t.getExtraKinetics()), helper.absolutePos(pos), pos, helper.getTick());
        }
    }

    public static <T extends KineticBlockEntity> void assertKineticsSpeed(final GameTestHelper helper, final BlockPos pos, final ToDoubleFunction<T> speed, final double delta) {
        SimulatedGameTestHelper.<T>assertBlockEntityData(helper, pos, be -> Math.abs(Math.abs(be.getSpeed()) - Math.abs(speed.applyAsDouble(be))) < delta, () -> {
            final T be = getBlockEntity(helper, pos);
            return "Expected %.2f speed, got %.2f".formatted(Math.abs(speed.applyAsDouble(be)), Math.abs(be.getSpeed()));
        });
    }

    public static <T extends KineticBlockEntity> void assertKineticsSpeed(final GameTestHelper helper, final BlockPos pos, final ToDoubleFunction<T> speed) {
        assertKineticsSpeed(helper, pos, speed, 1e-6);
    }

    public static void assertKineticsSpeed(final GameTestHelper helper, final BlockPos pos, final double speed, final double delta) {
        assertKineticsSpeed(helper, pos, be -> speed, delta);
    }

    public static void assertKineticsSpeed(final GameTestHelper helper, final BlockPos pos, final double speed) {
        assertKineticsSpeed(helper, pos, be -> speed, 1e-6);
    }

    public static <T extends KineticBlockEntity & ExtraKinetics> void assertExtraKineticsSpeed(final GameTestHelper helper, final BlockPos pos, final ToDoubleBiFunction<T, KineticBlockEntity> speed, final ToDoubleBiFunction<T, KineticBlockEntity> extraSpeed, final double delta) {
        SimulatedGameTestHelper.<T>assertExtraKinetics(helper, pos, (blockEntity, extraKinetics) -> Math.abs(Math.abs(blockEntity.getSpeed()) - speed.applyAsDouble(blockEntity, extraKinetics)) < delta &&
                        extraKinetics != null &&
                        Math.abs(Math.abs(extraKinetics.getSpeed()) - Math.abs(extraSpeed.applyAsDouble(blockEntity, extraKinetics))) < delta,
                (blockEntity, extraKinetics) -> {
                    if (extraKinetics == null) {
                        return "Expected extra kinetics, got null";
                    }
                    final double speedValue = Math.abs(speed.applyAsDouble(blockEntity, extraKinetics));
                    final double extraSpeedValue = Math.abs(extraSpeed.applyAsDouble(blockEntity, extraKinetics));
                    if (Math.abs(Math.abs(blockEntity.getSpeed()) - speedValue) >= delta) {
                        return "Expected %.2f speed, got %.2f".formatted(speedValue, Math.abs(blockEntity.getSpeed()));
                    }
                    return "Expected %.2f extra kinetics speed, got %.2f".formatted(Math.abs(extraSpeedValue), Math.abs(extraKinetics.getSpeed()));
                });
    }

    public static <T extends KineticBlockEntity & ExtraKinetics> void assertExtraKineticsSpeed(final GameTestHelper helper, final BlockPos pos, final ToDoubleBiFunction<T, KineticBlockEntity> speed, final ToDoubleBiFunction<T, KineticBlockEntity> extraSpeed) {
        assertExtraKineticsSpeed(helper, pos, speed, extraSpeed, 1e-6);
    }

    public static void assertExtraKineticsSpeed(final GameTestHelper helper, final BlockPos pos, final double speed, final double extraSpeed, final double delta) {
        assertExtraKineticsSpeed(helper, pos, (be, ebe) -> speed, (be, ebe) -> extraSpeed, 1e-6);
    }

    public static void assertExtraKineticsSpeed(final GameTestHelper helper, final BlockPos pos, final double speed, final double extraSpeed) {
        assertExtraKineticsSpeed(helper, pos, (be, ebe) -> speed, (be, ebe) -> extraSpeed, 1e-6);
    }
}
