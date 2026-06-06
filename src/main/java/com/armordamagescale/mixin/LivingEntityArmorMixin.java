package com.armordamagescale.mixin;

import com.armordamagescale.ArmorDamage;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.parser.ParseException;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.armordamagescale.config.CommonConfiguration.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityArmorMixin extends Entity
{
    public LivingEntityArmorMixin(EntityType<?> entityType, Level level)
    {
        super(entityType, level);
    }

    @Shadow
    public abstract void hurtArmor(DamageSource damageSource, float f);

    @Shadow
    public abstract int getArmorValue();

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract double getAttributeValue(Holder<Attribute> holder);

    @ModifyVariable(method = "actuallyHurt", argsOnly = true, at = @At("HEAD"), ordinal = 0)
    private float armordamage$onhurt(float damageOrg, final ServerLevel level, final DamageSource source, final float damage) throws EvaluationException, ParseException
    {
        if (source.getEntity() instanceof Player)
        {
            if (Float.isInfinite(damageOrg) || Float.isNaN(damageOrg) || damageOrg < 0)
            {
                ArmorDamage.LOGGER.warn("Bad damage value input:" + damageOrg, new Exception());
                return 0f;
            }

            final float normalizedDamage = ArmorDamage.config.getCommonConfig().playerdamagereduction.with(FORMULA_DAMAGE_ARG, damageOrg).evaluate().getNumberValue().floatValue();
            if (ArmorDamage.config.getCommonConfig().debugprint)
            {
                ArmorDamage.LOGGER.info("Normalizing player damage from: " + damage + " to:" + normalizedDamage);
            }

            return normalizedDamage;
        }

        return damageOrg;
    }

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), cancellable = true)
    private void armordamage$getDamageAfterArmorAbsorb(DamageSource damageSource, float damage, CallbackInfoReturnable<Float> cir) throws EvaluationException, ParseException
    {
        if (!damageSource.is(DamageTypeTags.BYPASSES_ARMOR))
        {
            if (Float.isInfinite(damage) || Float.isNaN(damage))
            {
                ArmorDamage.LOGGER.warn("Bad damage value input:" + damage, new Exception());
                cir.setReturnValue(0f);
                return;
            }

            String log = "";
            if (ArmorDamage.config.getCommonConfig().debugprint)
            {
                log += "Calculating damage for attack: origin:"+damageSource.getEntity()+" target:"+this+", dmgtype:"+damageSource.getMsgId()+", dmg:" + damage;
            }

            hurtArmor(damageSource, damage);
            final float armorValue = getArmorValue();

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

            final float toughness = (float) getAttributeValue(Attributes.ARMOR_TOUGHNESS);

            if (toughness > 0)
            {
                final float hitpct = Math.max(0, Math.min(1, modamage / getMaxHealth()));
                ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.with(FORMULA_TOUGHNESS_ARG, toughness);
                ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.with(FORMULA_HITPCT_ARG, hitpct);
                ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.with(FORMULA_DAMAGE_ARG, modamage);
                modamage = ArmorDamage.config.getCommonConfig().thoughnessdamagereduction.evaluate().getNumberValue().floatValue();

                if (ArmorDamage.config.getCommonConfig().debugprint)
                {
                    log += " Toughnessvalue:" + toughness + " dmg after toughness reduction:" + modamage;
                }
            }
            cir.setReturnValue(Math.max(0.5f, modamage));

            if (ArmorDamage.config.getCommonConfig().debugprint && !log.isEmpty())
            {
                ArmorDamage.LOGGER.info(log);
            }
        }
    }
}
