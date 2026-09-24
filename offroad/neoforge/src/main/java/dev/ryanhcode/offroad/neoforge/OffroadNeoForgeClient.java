package dev.ryanhcode.offroad.neoforge;

import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.OffroadClient;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.ConfigScreenHandler;

@Mod(value = Offroad.MOD_ID, dist = Dist.CLIENT)
public class OffroadNeoForgeClient {
	public OffroadNeoForgeClient(final IEventBus modBus, final ModContainer container) {
		this.listenClientEvents(modBus);
		container.registerExtensionPoint(IConfigScreenFactory.class, ((c, l) -> new BaseConfigScreen(l, Offroad.MOD_ID)));

		OffroadClient.init();
	}

	private void listenClientEvents(final IEventBus modBus) {

	}
}
