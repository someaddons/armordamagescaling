package com.armordamagescale.mixin;

import com.armordamagescale.ArmorDamage;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.parser.ParseException;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.armordamagescale.config.CommonConfiguration.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityArmorMixin
{
    @Shadow
    protected abstract void hurtArmor(DamageSource damageSource, float f);

    @Shadow
    public abstract int getArmorValue();

    @Shadow
    public abstract double getAttributeValue(Attribute attribute);

    @Shadow
    public abstract float getMaxHealth();

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), cancellable = true)
    private void armordamage$getDamageAfterArmorAbsorb(DamageSource damageSource, final float damage, CallbackInfoReturnable<Float> cir) throws EvaluationException, ParseException
    {
        if (!damageSource.isBypassArmor())
        {
            hurtArmor(damageSource, damage);
            final float armorValue = getArmorValue();

            ArmorDamage.config.getCommonConfig().armordamagereduction.with(FORMULA_ARMOR_ARG, armorValue);
            float modamage = 0;

            modamage = (float) (damage * ArmorDamage.config.getCommonConfig().armordamagereduction.evaluate().getNumberValue().floatValue());


            final float toughness = (float) getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            final float hitpct = Math.max(0, Math.min(1, modamage / getMaxHealth()));

            ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.with(FORMULA_TOUGHNESS_ARG, toughness);
            ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.with(FORMULA_HITPCT_ARG, hitpct);
            modamage *= ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.evaluate().getNumberValue().floatValue();

            cir.setReturnValue(modamage);

            if (ArmorDamage.config.getCommonConfig().debugprint)
            {
                ArmorDamage.LOGGER.info("Calculating damage: " + damage + " armor:" + armorValue + " reduction:" + ArmorDamage.config.getCommonConfig().armordamagereduction.evaluate().getNumberValue().floatValue() +
                        " toughness:" + toughness + " reduction:" + ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.evaluate().getNumberValue().floatValue() + " to final damage:" + modamage +
                        " Vanilla damage:" + CombatRules.getDamageAfterAbsorb(damage, (float) this.getArmorValue(), (float) this.getAttributeValue(Attributes.ARMOR_TOUGHNESS)));
            }
        }
    }
}
