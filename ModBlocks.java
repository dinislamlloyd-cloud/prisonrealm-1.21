package com.prisonrealm;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModBlocks {
	public static final Block PRISON_BRICKS = register("prison_bricks",
			AbstractBlock.Settings.copy(Blocks.STONE_BRICKS), true);

	public static final Block PRISON_LAMP = register("prison_lamp",
			AbstractBlock.Settings.copy(Blocks.GLOWSTONE).luminance(state -> 15), true);

	// Блоки только для анимации куба: без предметов, в мире не ставятся.
	/** Красная плоть с глазами: кубы в воздухе и грани запечатывающего куба. */
	public static final Block SEAL_FLESH = register("seal_flesh",
			AbstractBlock.Settings.copy(Blocks.NETHER_WART_BLOCK), false);

	/** Большой глаз в центре печати-креста. */
	public static final Block SEAL_EYE = register("seal_eye",
			AbstractBlock.Settings.copy(Blocks.NETHER_WART_BLOCK), false);

	/** Красная плоть без глаза: лапы и кубы на концах креста. */
	public static final Block SEAL_SKIN = register("seal_skin",
			AbstractBlock.Settings.copy(Blocks.NETHER_WART_BLOCK), false);

	/** Тёмно-красные щупальца. */
	public static final Block SEAL_TENDON = register("seal_tendon",
			AbstractBlock.Settings.copy(Blocks.NETHER_WART_BLOCK), false);

	/** Голубой куб, в котором сидит запечатанный. */
	public static final Block SEALED_CUBE = register("sealed_cube",
			AbstractBlock.Settings.copy(Blocks.LIGHT_BLUE_CONCRETE), false);

	private ModBlocks() {
	}

	private static Block register(String name, AbstractBlock.Settings settings, boolean withItem) {
		Identifier id = PrisonRealmMod.id(name);
		Block block = new Block(settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)));
		Registry.register(Registries.BLOCK, id, block);
		if (withItem) {
			Item.Settings itemSettings = new Item.Settings()
					.registryKey(RegistryKey.of(RegistryKeys.ITEM, id))
					.useBlockPrefixedTranslationKey();
			Registry.register(Registries.ITEM, id, new BlockItem(block, itemSettings));
		}
		return block;
	}

	public static void register() {
		// Вызов нужен, чтобы загрузился класс и выполнились статические инициализаторы.
	}
}
