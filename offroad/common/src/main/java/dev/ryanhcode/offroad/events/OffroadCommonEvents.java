package dev.ryanhcode.offroad.events;

import com.simibubi.create.AllBlocks;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.handlers.client.MultiMiningClientHandler;
import dev.ryanhcode.offroad.handlers.server.MultiMiningServerManager;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class OffroadCommonEvents {

    /**
     * 1.20.1: replaces the default data components 1.21 added to Create's items through ModifyDefaultComponentsEvent.
     */
    public static void registerDefaultComponents() {
        OffroadDataComponents.registerDefaultTire(AllBlocks.FLYWHEEL, TireLike.FLYWHEEL);
        OffroadDataComponents.registerDefaultTire(AllBlocks.LARGE_WATER_WHEEL, TireLike.LARGE_WATER_WHEEL);
        OffroadDataComponents.registerDefaultTire(AllBlocks.CRUSHING_WHEEL, TireLike.CRUSHING_WHEEL);
        OffroadDataComponents.registerDefaultTire(AllBlocks.WATER_WHEEL, TireLike.WATER_WHEEL);
        OffroadDataComponents.registerDefaultTire(AllBlocks.MECHANICAL_ROLLER, TireLike.MECHANICAL_ROLLER);
    }

    public static void physicsTick(final SubLevelPhysicsSystem physicsSystem, final double timeStep) {
        final ServerLevel level = physicsSystem.getLevel();
        WheelMountBlockEntity.applyAllBatchedForces(level, timeStep);
    }

    public static void tickLevelEvent(final Level level) {
        if (!level.isClientSide) {
            MultiMiningServerManager.tick(level);
        } else {
            MultiMiningClientHandler.tick(level);
        }
    }
}
