package com.prisonrealm.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Дубинка стражника: слабый урон, но замедляет цель, чтобы можно было надеть наручники. */
public class GuardBatonItem extends Item {
	public GuardBatonItem(Settings settings) {
		super(settings);
	}

	@Override
	public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2), attacker);
		super.postHit(stack, target, attacker);
	}
}
