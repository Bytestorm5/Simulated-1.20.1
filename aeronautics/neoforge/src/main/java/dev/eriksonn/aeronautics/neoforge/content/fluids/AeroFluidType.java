package dev.eriksonn.aeronautics.neoforge.content.fluids;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tterrag.registrate.builders.FluidBuilder;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AeroFluidType extends FluidType {
	private Vector3f fogColor;
	private Supplier<Float> fogDistance;
	private final ResourceLocation stillTexture;
	private final ResourceLocation flowingTexture;

	public AeroFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
		super(properties);
		this.stillTexture = stillTexture;
		this.flowingTexture = flowingTexture;
	}

	public static FluidBuilder.FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance, Factory factory) {
		return (p, s, f) -> {
			AeroFluidType fluidType = factory.create(p, s, f);
			fluidType.fogColor = new Color(fogColor, false).asVectorF();
			fluidType.fogDistance = fogDistance;
			return fluidType;
		};
	}

	public interface Factory {
		AeroFluidType create(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture);
	}

	/**
	 * 1.20.1: client fluid type extensions are supplied here instead of through RegisterClientExtensionsEvent
	 */
	@Override
	public void initializeClient(final Consumer<IClientFluidTypeExtensions> consumer) {
		consumer.accept(new IClientFluidTypeExtensions() {
			@Override
			public @NotNull ResourceLocation getStillTexture() {
				return AeroFluidType.this.stillTexture;
			}

			@Override
			public @NotNull ResourceLocation getFlowingTexture() {
				return AeroFluidType.this.flowingTexture;
			}

			@Override
			public @NotNull Vector3f modifyFogColor(final Camera camera, final float partialTick, final ClientLevel level, final int renderDistance, final float darkenWorldAmount, final Vector3f fluidFogColor) {
				final Vector3f customFogColor = AeroFluidType.this.getCustomFogColor();
				return customFogColor == null ? fluidFogColor : customFogColor;
			}

			@Override
			public void modifyFogRender(final Camera camera, final FogRenderer.FogMode mode, final float renderDistance, final float partialTick, final float nearDistance, final float farDistance, final FogShape shape) {
				IClientFluidTypeExtensions.super.modifyFogRender(camera, mode, renderDistance, partialTick, nearDistance, farDistance, shape);
				final float modifier = AeroFluidType.this.getFogDistanceModifier();
				final float baseWaterFog = 96.0f;
				if (modifier != 1.0f) {
					RenderSystem.setShaderFogShape(FogShape.CYLINDER);
					RenderSystem.setShaderFogStart(-8);
					RenderSystem.setShaderFogEnd(baseWaterFog * modifier);
				}
			}
		});
	}

	public Vector3f getCustomFogColor() {
		return this.fogColor;
	}

	public float getFogDistanceModifier() {
		return this.fogDistance.get();
	}
}
