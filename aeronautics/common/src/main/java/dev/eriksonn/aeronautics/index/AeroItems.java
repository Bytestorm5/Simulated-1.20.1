package dev.eriksonn.aeronautics.index;

import com.simibubi.create.AllItems;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.components.Levitating;
import dev.eriksonn.aeronautics.content.items.AviatorsGogglesItem;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.Rarity;

public class AeroItems {
	private static final SimulatedRegistrate REGISTRATE = Aeronautics.getRegistrate();

	public static final ItemEntry<AviatorsGogglesItem> AVIATORS_GOGGLES = REGISTRATE
					.item("aviators_goggles", AviatorsGogglesItem::new)
					.lang("Aviator's Goggles")
					.recipe((c, p) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, c.get(), 1)
							.requires(AeroTags.ItemTags.LEATHERS)
							.requires(AllItems.GOGGLES)
							.unlockedBy("has_ingredient", RegistrateRecipeProvider.has(AllItems.GOGGLES))
							.save(p))
					.tag(AeroTags.ItemTags.ARMORS)
					.tag(AeroTags.ItemTags.HEAD_ARMOR)
					.tag(ItemTags.FREEZE_IMMUNE_WEARABLES)
					.register();

	// 1.20.1: no jukebox song registry, music discs are RecordItems (comparator output 12, 225 seconds long)
	public static ItemEntry<RecordItem> MUSIC_DISC_CLOUD_SKIPPER =
			REGISTRATE.item("music_disc_cloud_skipper", p -> new RecordItem(12, AeroSoundEvents.MUSIC_DISC_CLOUD_SKIPPER.registryObject(), p, 225 * 20))
					.properties(p -> p
							.stacksTo(1)
							.rarity(Rarity.RARE)
					)
					.onRegister(item -> AeroDataComponents.setDefaultLevitating(item, Levitating.DEFAULT))
					.tag(AeroTags.ItemTags.MUSIC_DISCS)
					.lang("Music Disc")
					.register();

	public static ItemEntry<Item> ENDSTONE_POWDER = REGISTRATE.item("end_stone_powder", Item::new)
			.onRegister(item -> AeroDataComponents.setDefaultLevitating(item, Levitating.END_STONE))
			.register();

	public static void init() {}
}
