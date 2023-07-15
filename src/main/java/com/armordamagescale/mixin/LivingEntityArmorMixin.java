package com.armordamagescale.mixin;

import com.armordamagescale.ArmorDamage;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.parser.ParseException;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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

    @ModifyVariable(method = "actuallyHurt", argsOnly = true, at = @At("HEAD"), ordinal = 0)
    private float brutalbosses$onhurt(float damageOrg, final DamageSource source, final float damage) throws EvaluationException, ParseException
    {
        if (source.getEntity() instanceof Player)
        {
            return ArmorDamage.config.getCommonConfig().playerdamagereduction.with(FORMULA_DAMAGE_ARG, damageOrg).evaluate().getNumberValue().floatValue();
        }

        return damageOrg;
    }

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), cancellable = true)
    private void armordamage$getDamageAfterArmorAbsorb(DamageSource damageSource, float damage, CallbackInfoReturnable<Float> cir) throws EvaluationException, ParseException
    {
        if (!damageSource.isBypassArmor())
        {
            if (Float.isInfinite(damage) || Float.isNaN(damage))
            {
                ArmorDamage.LOGGER.warn("Bad damage value input:" + damage, new Exception());
                cir.setReturnValue(0f);
                return;
            }


            hurtArmor(damageSource, damage);
            final float armorValue = getArmorValue();

            float modamage = damage;

            if (armorValue > 0)
            {
                modamage = ArmorDamage.config.getCommonConfig().armordamagereduction.with(FORMULA_ARMOR_ARG, armorValue).with(FORMULA_DAMAGE_ARG, damage)
                        .evaluate().getNumberValue().floatValue();
            }

            final float toughness = (float) getAttributeValue(Attributes.ARMOR_TOUGHNESS);

            if (toughness > 0)
            {
                final float hitpct = Math.max(0, Math.min(1, modamage / getMaxHealth()));
                ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.with(FORMULA_TOUGHNESS_ARG, toughness);
                ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.with(FORMULA_HITPCT_ARG, hitpct);
                ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.with(FORMULA_DAMAGE_ARG, modamage);
                modamage = ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.evaluate().getNumberValue().floatValue();
            }
            cir.setReturnValue(Math.max(0.5f, modamage));

            if (ArmorDamage.config.getCommonConfig().debugprint)
            {
                ArmorDamage.LOGGER.info("Calculating damage: " + damage + " armor:" + armorValue + " reduction:" + ArmorDamage.config.getCommonConfig().armordamagereduction.evaluate().getNumberValue().floatValue() +
                        " toughness:" + toughness + " reduction:" + ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.evaluate().getNumberValue().floatValue() + " to final damage:" + modamage +
                        " Vanilla damage:" + CombatRules.getDamageAfterAbsorb(damage, (float) this.getArmorValue(), (float) this.getAttributeValue(Attributes.ARMOR_TOUGHNESS)));
            }
        }
    }
}
