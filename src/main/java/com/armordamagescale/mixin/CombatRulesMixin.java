package com.armordamagescale.mixin;

import com.armordamagescale.ArmorDamage;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.parser.ParseException;
import net.minecraft.world.damagesource.CombatRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.armordamagescale.config.CommonConfiguration.*;

@Mixin(CombatRules.class)
public class CombatRulesMixin
{
    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"), cancellable = true)
    private static void adjustValue(final float damage, final float totalMagicArmor, final CallbackInfoReturnable<Float> cir) throws EvaluationException, ParseException
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
        if (damage > 0 && totalMagicArmor > 0)
        {
            cir.setReturnValue(ArmorDamage.config.getCommonConfig().protectionReduction.with(FORMULA_PROTECTION_ARG, totalMagicArmor)
                .with(FORMULA_DAMAGE_ARG, damage)
                .evaluate()
                .getNumberValue()
                .floatValue());
        }
    }
}
