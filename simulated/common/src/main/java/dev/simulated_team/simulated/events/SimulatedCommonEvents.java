package dev.simulated_team.simulated.events;

import com.simibubi.create.AllItems;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.index.SableAttributes;
import dev.ryanhcode.sable.platform.SableEventPlatform;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.ryanhcode.sable.sublevel.system.SubLevelTrackingSystem;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone_magnet.RedstoneMagnetBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerLevelRopeManager;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeTrackingSystem;
import dev.simulated_team.simulated.content.blocks.spring.SpringBlock;
import dev.simulated_team.simulated.content.end_sea.EndSeaPhysicsData;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.launched_plunger.LaunchedPlungerServerHandler;
import dev.simulated_team.simulated.content.navigation_targets.lodestone_compass_compatability.LodestoneTrackingMap;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffServerHandler;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffSubLevelObserver;
import dev.simulated_team.simulated.content.worldgen.SimulatedWorldPreset;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.index.SimTags;
import dev.simulated_team.simulated.index.SimWorldPresets;
import dev.simulated_team.simulated.mixin_interface.PrimaryLevelDataExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class SimulatedCommonEvents {
    /**
     * Called at the end of the given ServerLevel's tick
     *
     * @param level The level ticking.
     */
    public static void onServerTickEnd(final ServerLevel level) {
        RedstoneMagnetBlockEntity.GLOBAL_REDSTONE_MAGNET_MAP.tick(level);
        DockingConnectorBlockEntity.MAGNET_CONTROLLER.tick(level);

	    final LodestoneTrackingMap lodestoneMap = LodestoneTrackingMap.getOrLoad(level);
		if (lodestoneMap != null) {
			lodestoneMap.tick();
		}
    }

    public static void onWorldLoad(final LevelAccessor level) {

    }

    /**
     * Called whenever a block is modified in the given level
     */
    public static void onBlockModifiedEvent(final LevelAccessor level, final BlockPos breakPos) {
    }

    public static void onServerStopped(final MinecraftServer server) {

    }

    public static void onPlayerLoggedIn(final Player player) {
        for (final Map.Entry<ResourceLocation, SimulatedWorldPreset> entry : SimWorldPresets.PRESETS.entrySet()) {
            final ServerLevel level = (ServerLevel) player.level();

            if (player instanceof final ServerPlayer serverPlayer) {
                final ResourceLocation worldPreset = ((PrimaryLevelDataExtension) level.getServer().getWorldData()).getPreset();

                if (entry.getValue().id().equals(worldPreset)) {
                    entry.getValue().onPlayerJoin(level, serverPlayer);
                }
            }
        }

        PhysicsStaffServerHandler.sendAllData(player);
    }

    public static void onChunkLoad(final LevelAccessor level, final ChunkAccess chunk, final boolean newChunk) {
        for (final Map.Entry<ResourceLocation, SimulatedWorldPreset> entry : SimWorldPresets.PRESETS.entrySet()) {
            if(level instanceof final ServerLevel serverLevel) {
                final ResourceLocation worldPreset = ((PrimaryLevelDataExtension) serverLevel.getServer().getWorldData()).getPreset();

                if(entry.getValue().id().equals(worldPreset)) {
                    entry.getValue().onChunkLoad(serverLevel, chunk, newChunk);
                }
            }
        }
    }

    public static void onPhysicsTick(final SubLevelPhysicsSystem physicsSystem, final double timeStep) {
        final ServerLevel level = physicsSystem.getLevel();
        RedstoneMagnetBlockEntity.GLOBAL_REDSTONE_MAGNET_MAP.physicsTick(timeStep, level);
        DockingConnectorBlockEntity.MAGNET_CONTROLLER.physicsTick(timeStep, level);
        EndSeaPhysicsData.physicsTick(timeStep, level);
        ServerLevelRopeManager.getOrCreate(level).physicsTick(physicsSystem, timeStep);

        LaunchedPlungerServerHandler.physicsTickAllPlungers(physicsSystem, timeStep);
        PhysicsStaffServerHandler.get(level).physicsTick(physicsSystem);
    }

    public static @Nullable InteractionResult rightClickBlock(final Level level, final BlockPos pos, final Player player, final ItemStack useStack) {
        if (SimBlocks.SPRING.has(level.getBlockState(pos)) && useStack.is(SimTags.Items.SPRING_ADJUSTER)) {
            if (SpringBlock.tryAdjustSpring(level, pos, player)) {
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.FAIL;
            }
        }
        return null;
    }

    public static void onPostPhysicsTick(final SubLevelPhysicsSystem physicsSystem, final double timeStep) {
        DiagramEntity.postPhysicsTick(physicsSystem.getLevel());
    }

    private static void onContainerReady(final Level level, final SubLevelContainer subLevelContainer) {
        if (!(subLevelContainer instanceof final ServerSubLevelContainer serverContainer)) {
            return;
        }

        serverContainer.addObserver(new PhysicsStaffSubLevelObserver(serverContainer.getLevel()));
        
        final SubLevelTrackingSystem trackingSystem = serverContainer.trackingSystem();
        trackingSystem.addTrackingPlugin(new ServerRopeTrackingSystem(serverContainer.getLevel()));
    }

    public static void register() {
        SableEventPlatform.INSTANCE.onSubLevelContainerReady(SimulatedCommonEvents::onContainerReady);
    }

    private static final UUID BASE_PUNCH_STRENGTH_ID = modifierId(Simulated.path("base_punch_strength"));
    private static final UUID BASE_PUNCH_COOLDOWN_ID = modifierId(Simulated.path("base_punch_cooldown"));

    private static UUID modifierId(final ResourceLocation id) {
        return UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Adds Simulated's attribute modifiers to items of other mods.
     * <p>
     * 1.20.1: items have no default {@code ATTRIBUTE_MODIFIERS} component, so the loader calls this from its item
     * attribute modifier event for every stack and slot, and the modifiers are added to the item's existing ones
     * (on 1.21 they replaced the default component).
     *
     * @param stack       The stack whose attribute modifiers are being gathered
     * @param slot        The slot the modifiers are for
     * @param addModifier Adds a modifier to the stack's modifiers
     */
    public static void modifyItemAttributes(final ItemStack stack, final EquipmentSlot slot, final BiConsumer<Attribute, AttributeModifier> addModifier) {
        if (slot != EquipmentSlot.MAINHAND) {
            return;
        }

        if (AllItems.EXTENDO_GRIP.isIn(stack)) {
            final AttributeModifier strengthModifier = new AttributeModifier(BASE_PUNCH_STRENGTH_ID, "base_punch_strength", 10.0f, AttributeModifier.Operation.MULTIPLY_TOTAL);
            final AttributeModifier cooldownModifier = new AttributeModifier(BASE_PUNCH_COOLDOWN_ID, "base_punch_cooldown", 0.5f, AttributeModifier.Operation.ADDITION);

            addModifier.accept(SableAttributes.PUNCH_STRENGTH.get(), strengthModifier);
            addModifier.accept(SableAttributes.PUNCH_COOLDOWN.get(), cooldownModifier);
        }

        if (AllItems.CARDBOARD_SWORD.isIn(stack)) {
            final AttributeModifier attributeModifier = new AttributeModifier(BASE_PUNCH_STRENGTH_ID, "base_punch_strength", 2.0f, AttributeModifier.Operation.MULTIPLY_TOTAL);

            addModifier.accept(SableAttributes.PUNCH_STRENGTH.get(), attributeModifier);
        }
    }
}
