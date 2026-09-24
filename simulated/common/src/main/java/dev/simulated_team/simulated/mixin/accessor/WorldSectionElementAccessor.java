package dev.simulated_team.simulated.mixin.accessor;

import net.createmod.ponder.foundation.element.WorldSectionElementImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = WorldSectionElementImpl.class, remap = false)
public interface WorldSectionElementAccessor {

    @Accessor
    Vec3 getCenterOfRotation();
}
