package com.armordamagescale.mixin;

import com.armordamagescale.ArmorDamage;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.parser.ParseException;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.armordamagescale.config.CommonConfiguration.*;

@Mixin(CombatRules.class)
public class CombatRulesMixin
{
    @ModifyReturnValue(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"))
    private static float adjustValue(final float original, float damage, float totalMagicArmor) throws EvaluationException, ParseException
    {
        if (ArmorDamage.config.getCommonConfig().debugprint)
        {
            ArmorDamage.LOGGER.info("damage before protection enchant:" + damage + " protection total level:" + totalMagicArmor + " damage after:"
                + (ArmorDamage.config.getCommonConfig().protectionReduction.with(FORMULA_PROTECTION_ARG, totalMagicArmor)
                .with(FORMULA_DAMAGE_ARG, damage)
                .evaluate()
                .getNumberValue()
                .floatValue()));
        }
        return ArmorDamage.config.getCommonConfig().protectionReduction.with(FORMULA_PROTECTION_ARG, totalMagicArmor)
            .with(FORMULA_DAMAGE_ARG, damage)
            .evaluate()
            .getNumberValue()
            .floatValue();
    }

    @Inject(method = "getDamageAfterAbsorb", at = @At("HEAD"), cancellable = true)
    private static void armordamage$getDamageAfterArmorAbsorb(
        final LivingEntity victim,
        final float damage,
        final DamageSource damageSource,
        final float armorValue,
        final float toughness,
        final CallbackInfoReturnable<Float> cir) throws EvaluationException, ParseException
    {
        if (Float.isInfinite(damage) || Float.isNaN(damage))
        {
            ArmorDamage.LOGGER.warn("Bad damage value input:" + damage, new Exception());
            cir.setReturnValue(0f);
            return;
        }

        if (damage <= 0 || victim == null)
        {
            return;
        }

        String log = "";
        if (ArmorDamage.config.getCommonConfig().debugprint)
        {
            log += "Calculating damage for attack: origin:" + damageSource.getEntity() + " target:" + victim + ", dmgtype:" + damageSource.getMsgId() + ", dmg:" + damage;
        }
        float modamage = damage;

        if (armorValue > 0)
        {
            modamage = ArmorDamage.config.getCommonConfig().armordamagereduction.with(FORMULA_ARMOR_ARG, armorValue).with(FORMULA_DAMAGE_ARG, damage)
                .evaluate().getNumberValue().floatValue();
            if (ArmorDamage.config.getCommonConfig().debugprint)
            {
                log += ", Armorvalue:" + armorValue + ", dmg after armor reduction:" + modamage;
            }
        }

        if (toughness > 0)
        {
            final float hitpct = Math.max(0, Math.min(1, modamage / victim.getMaxHealth()));
            ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.with(FORMULA_TOUGHNESS_ARG, toughness);
            ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.with(FORMULA_HITPCT_ARG, hitpct);
            ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.with(FORMULA_DAMAGE_ARG, modamage);
            modamage = ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.evaluate().getNumberValue().floatValue();

            if (ArmorDamage.config.getCommonConfig().debugprint)
            {
                log += " Toughnessvalue:" + toughness + " dmg after toughness reduction:" + modamage;
            }
        }

        // TODO: Recheck vanilla logic when minecraft versions change
        final ItemStack weaponItem = damageSource.getWeaponItem();
        if (weaponItem != null && victim.level() instanceof ServerLevel level)
        {
            float damageReduction = 1.0f - (modamage / damage);
            damageReduction = Mth.clamp(EnchantmentHelper.modifyArmorEffectiveness(level, weaponItem, victim, damageSource, damageReduction), 0.0F, 1.0F);
            modamage = damage * (1.0f - damageReduction);
            if (ArmorDamage.config.getCommonConfig().debugprint)
            {
                log += " Enchantment armor effectiveness reduction, dmg after:" + modamage;
            }
        }

        cir.setReturnValue(Math.max(0.0f, modamage));

        if (ArmorDamage.config.getCommonConfig().debugprint && !log.isEmpty())
        {
            ArmorDamage.LOGGER.info(log);
        }
    }
}
