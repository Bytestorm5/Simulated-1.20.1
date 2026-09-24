package dev.eriksonn.aeronautics.index;

import dev.eriksonn.aeronautics.Aeronautics;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

/**
 * 1.20.1: armor materials are plain {@link ArmorMaterial} implementations instead of registry entries.
 */
public enum AeroArmorMaterials implements ArmorMaterial {
	AVIATORS_GOGGLES(Aeronautics.path("aviators_goggles").toString(), new int[] { 1, 0, 0, 0 }, 15,
			() -> SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(Items.LEATHER), 0.0f, 0.0f);

	private final String name;
	/**
	 * Defense per armor type, indexed by {@link ArmorItem.Type} ordinal (helmet, chestplate, leggings, boots)
	 */
	private final int[] defense;
	private final int enchantmentValue;
	private final Supplier<SoundEvent> equipSound;
	private final Supplier<Ingredient> repairIngredient;
	private final float toughness;
	private final float knockbackResistance;

	AeroArmorMaterials(final String name, final int[] defense, final int enchantmentValue, final Supplier<SoundEvent> equipSound,
					   final Supplier<Ingredient> repairIngredient, final float toughness, final float knockbackResistance) {
		this.name = name;
		this.defense = defense;
		this.enchantmentValue = enchantmentValue;
		this.equipSound = equipSound;
		this.repairIngredient = repairIngredient;
		this.toughness = toughness;
		this.knockbackResistance = knockbackResistance;
	}

	@Override
	public int getDurabilityForType(final ArmorItem.Type type) {
		// The 1.21 material gave no durability, keep the item unbreakable
		return 0;
	}

	@Override
	public int getDefenseForType(final ArmorItem.Type type) {
		return switch (type) {
			case HELMET -> this.defense[0];
			case CHESTPLATE -> this.defense[1];
			case LEGGINGS -> this.defense[2];
			case BOOTS -> this.defense[3];
		};
	}

	@Override
	public int getEnchantmentValue() {
		return this.enchantmentValue;
	}

	@Override
	public SoundEvent getEquipSound() {
		return this.equipSound.get();
	}

	@Override
	public Ingredient getRepairIngredient() {
		return this.repairIngredient.get();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public float getToughness() {
		return this.toughness;
	}

	@Override
	public float getKnockbackResistance() {
		return this.knockbackResistance;
	}

	public static void init() {}
}
