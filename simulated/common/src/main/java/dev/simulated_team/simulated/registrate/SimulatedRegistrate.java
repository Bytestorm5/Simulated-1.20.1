package dev.simulated_team.simulated.registrate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.simulated_team.simulated.client.BlockPropertiesTooltip;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import dev.simulated_team.simulated.index.SimRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class SimulatedRegistrate extends CreateRegistrate {

    public static final Set<String> MODS = new HashSet<>();
    public static final List<Supplier<Item>> TAB_ITEMS = Collections.synchronizedList(new ArrayList<>());
    public static final Map<ResourceLocation, ResourceLocation> ITEM_TO_SECTION = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, Supplier<ItemLike>> NAVIGATION_TARGET_ITEMS = new ConcurrentHashMap<>();

    private ResourceLocation currentSection;

    public SimulatedRegistrate(final ResourceLocation initialSection, final String modId) {
        super(modId);
        this.currentSection = initialSection;
        MODS.add(modId);
    }

    public SimulatedRegistrate inSection(final ResourceLocation section) {
        this.currentSection = section;
        return this;
    }

    public <T> Codec<T> byNameCodecExpanded(final ResourceKey<? extends Registry<T>> key) {
        return ResourceLocation.CODEC.flatXmap((resourceLoc) -> {
            T gatheredEntry = null;
            for (final RegistryEntry<T> entry : this.getAll(key)) {
                if (entry.getId().equals(resourceLoc)) {
                    gatheredEntry = entry.get();
                    break;
                }
            }

            if (gatheredEntry != null) {
                return DataResult.success(gatheredEntry);
            } else {
                return DataResult.error(() -> "Unknown registry element in " + key + ":" + resourceLoc);
            }
        }, (T) -> {
            ResourceLocation id = null;
            for (final RegistryEntry<T> entry : this.getAll(key)) {
                if (entry.is(T)) {
                    id = entry.getId();
                    break;
                }
            }

            if (id != null) {
                return DataResult.success(id);
            } else {
                return DataResult.error(() -> "Unknown registry element in " + key + ":" + T);
            }
        });
    }

    public static ResourceLocation sectionOf(final Item item) {
        return ITEM_TO_SECTION.get(BuiltInRegistries.ITEM.getKey(item));
    }

    @Override
    protected <R, T extends R> @NotNull RegistryEntry<T> accept(final String name, final ResourceKey<? extends Registry<R>> type, final Builder<R, T, ?, ?> builder, final NonNullSupplier<? extends T> creator, final NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {
        final RegistryEntry<T> entry = super.accept(name, type, builder, creator, entryFactory);

        if (type.equals(Registries.ITEM)) {
            final RegistryEntry<? extends Item> itemEntry = (RegistryEntry<? extends Item>) entry;
            TAB_ITEMS.add(itemEntry::get);
            ITEM_TO_SECTION.put(entry.getId(), this.currentSection);
        }

        return entry;
    }

    public void addExtraItem(final ResourceLocation item) {
        TAB_ITEMS.add(() -> BuiltInRegistries.ITEM.get(item));
        ITEM_TO_SECTION.put(item, this.currentSection);
    }

    public <T extends NavigationTarget> RegistryEntry<T> navTarget(final String name, final NonNullSupplier<T> navTableItem, Supplier<ItemLike> itemSupplier) {
        RegistryEntry<T> entry = this.simple(this.self(), name, SimRegistries.Keys.NAVIGATION_TARGET, navTableItem);
        NAVIGATION_TARGET_ITEMS.put(entry.getId(), itemSupplier);
        return entry;
    }

    public <T extends NavigationTarget> RegistryEntry<T> navTarget(final String name, final NonNullSupplier<T> navTableItem, ItemLike item) {
        return navTarget(name, navTableItem, () -> item);
    }

    public <T extends BlockPropertiesTooltip.Entry> RegistryEntry<T>
            propertyTooltip(final String name, final NonNullSupplier<T> tooltipFunction) {
        return this.simple(this.self(), name, SimRegistries.Keys.PROPERTY_TOOLTIP, tooltipFunction);
    }

    /**
     * 1.20.1: items have no default data components, so the navigation target that 1.21 set as the default
     * {@code TARGET} component of a navigation target's item is looked up here instead.
     *
     * @return The navigation target registered for the given item, or null if there is none
     */
    public static @Nullable NavigationTarget getDefaultNavigationTarget(final ItemLike item) {
        final Item asItem = item.asItem();
        for (final Map.Entry<ResourceLocation, Supplier<ItemLike>> entry : NAVIGATION_TARGET_ITEMS.entrySet()) {
            if (entry.getValue().get().asItem() == asItem) {
                return SimRegistries.NAVIGATION_TARGET.get(entry.getKey());
            }
        }
        return null;
    }
}
