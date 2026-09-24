package dev.simulated_team.simulated.mixin.schematicannon_fix;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// todo: delete this when updating create
@Mixin(value = SchematicPrinter.class, remap = false)
public class SchematicPrinterMixin {
    @Shadow private SchematicLevel blockReader;

    // 1.20.1: Create 6.0.8 has the same bug (it reads the data of the freshly created, empty block entity)
    @Redirect(method = "getCurrentRequirement", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/utility/BlockHelper;prepareBlockEntityData(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag fixBlockRequirements(final BlockState block, final BlockEntity _blockEntity, @Local(name = "target") final BlockPos target) {
        return BlockHelper.prepareBlockEntityData(block, this.blockReader.getBlockEntity(target));
    }
}
