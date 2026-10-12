package com.prisonrealm.item;

import com.prisonrealm.prison.PrisonManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Запечатывающий куб.
 *  - ПКМ по игроку или мобу (или ПКМ в воздух, глядя на цель): куб хватает цель.
 *  - Ещё раз ПКМ (или «close» в чат): кубы смыкаются на цели.
 */
public class SealingCubeItem extends Item {
	public static final int COOLDOWN_TICKS = 20 * 20;

	public SealingCubeItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}
		if (user instanceof ServerPlayerEntity owner && PrisonManager.useCube(owner)) {
			return ActionResult.SUCCESS;
		}
		return ActionResult.FAIL;
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		if (user.getEntityWorld().isClient()) {
			return ActionResult.SUCCESS;
		}
		if (user instanceof ServerPlayerEntity owner && PrisonManager.useCubeOn(owner, entity)) {
			return ActionResult.SUCCESS;
		}
		return ActionResult.FAIL;
	}
}
