package dev.eriksonn.aeronautics.mixin.levitite;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.eriksonn.aeronautics.content.components.Levitating;
import dev.eriksonn.aeronautics.index.AeroDataComponents;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(final EntityType<?> entityType, final Level level) {
        super(entityType, level);
    }

    @Shadow public abstract ItemStack getItem();

    // 1.20.1: items have no getDefaultGravity, the gravity step in tick is skipped by reporting no gravity instead
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;isNoGravity()Z"))
    private boolean aeronautics$levitatingGravity(final boolean noGravity) {
        if (noGravity) {
            return true;
        }
        final Levitating component = AeroDataComponents.getLevitating(this.getItem());
        return component != null;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private void aeronautics$levitatingDragAndSparkles(final CallbackInfo ci) {
        final Levitating component = AeroDataComponents.getLevitating(this.getItem());
        if (component != null) {
            final float dragFraction = Mth.clamp(component.dragFraction(), 0, 1);
            this.setDeltaMovement(this.getDeltaMovement().scale(dragFraction));

            if (this.level().isClientSide && component.particle().isPresent()) {
                if (this.level().random.nextFloat() < Mth.clamp(this.getItem().getCount() - 10, 5, 100) / 64f) {
                    final Vec3 ppos = VecHelper.offsetRandomly(this.getPosition(0), this.random, 0.4f).add(0, 0.3, 0);
                    this.level().addParticle(component.particle().get(), ppos.x, ppos.y, ppos.z, 0, 0, 0);
                }
            }
        }
    }
}
