package dev.eriksonn.aeronautics.mixin.flammable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 1.20.1: {@link FireBlock#setFlammable} is private
 */
@Mixin(FireBlock.class)
public interface FireBlockAccessor {
	@Invoker("setFlammable")
	void aeronautics$setFlammable(Block block, int encouragement, int flammability);
}
