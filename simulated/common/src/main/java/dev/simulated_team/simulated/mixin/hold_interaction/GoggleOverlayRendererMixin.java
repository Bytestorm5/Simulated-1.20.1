package dev.simulated_team.simulated.mixin.hold_interaction;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import dev.simulated_team.simulated.index.SimClickInteractions;
import foundry.veil.backport.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GoggleOverlayRenderer.class)
public class GoggleOverlayRendererMixin {
    @Shadow public static int hoverTicks;

    @Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", shift = At.Shift.BEFORE))
    private static void decrementRenderTicks(final CallbackInfo ci) {
        if (SimClickInteractions.STEERING_WHEEL_MANAGER.isActive()) {
            hoverTicks = Mth.clamp(hoverTicks - 2, 0, 24);
        }
    }

    @WrapOperation(method = "renderOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private static float fixPartialTicks(final float value, final float min, final float max, final Operation<Float> original) {
        if (SimClickInteractions.STEERING_WHEEL_MANAGER.isActive()) {
            return Mth.clamp(hoverTicks - DeltaTracker.current().getGameTimeDeltaTicks(), 0, 24) / 24;
        }
        return original.call(value, min, max);
    }

    @Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"), cancellable = true)
    private static void dontRenderTheText(final ForgeGui gui, final GuiGraphics guiGraphics, final float partialTicks, final int width, final int height, final CallbackInfo ci) {
        if (hoverTicks - DeltaTracker.current().getGameTimeDeltaTicks() <= 0) {
            ci.cancel();
        }
    }
}