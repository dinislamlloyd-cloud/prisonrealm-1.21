package com.prisonrealm;

import com.prisonrealm.item.GuardBatonItem;
import com.prisonrealm.item.HandcuffsItem;
import com.prisonrealm.item.SealingCubeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public final class ModItems {
	public static final Item HANDCUFFS = register("handcuffs", HandcuffsItem::new,
			new Item.Settings().maxDamage(16));

	public static final Item GUARD_BATON = register("guard_baton", GuardBatonItem::new,
			new Item.Settings().sword(ToolMaterial.WOOD, 1.0f, -2.2f));

	public static final Item SEALING_CUBE = register("sealing_cube", SealingCubeItem::new,
			new Item.Settings().maxCount(1));

	private ModItems() {
	}

	private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
		Identifier id = PrisonRealmMod.id(name);
		Item item = factory.apply(settings.registryKey(RegistryKey.of(RegistryKeys.ITEM, id)));
		return Registry.register(Registries.ITEM, id, item);
	}

	public static void register() {
		// Вызов нужен, чтобы загрузился класс и выполнились статические инициализаторы.
	}
}
