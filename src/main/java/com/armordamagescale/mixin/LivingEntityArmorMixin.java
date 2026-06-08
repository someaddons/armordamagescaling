package com.armordamagescale.mixin;

import com.armordamagescale.ArmorDamage;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.parser.ParseException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import javax.annotation.Nullable;
import java.util.Stack;

import static com.armordamagescale.config.CommonConfiguration.FORMULA_DAMAGE_ARG;

@Mixin(LivingEntity.class)
public abstract class LivingEntityArmorMixin extends Entity
{
    public LivingEntityArmorMixin(EntityType<?> entityType, Level level)
    {
        super(entityType, level);
    }

    @Shadow
    @Nullable
    protected Stack<DamageContainer> damageContainers;

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

            float normalizedDamage = damageOrg;
            if (!damageContainers.empty())
            {
                damageOrg = damageContainers.peek().getNewDamage();
                normalizedDamage = ArmorDamage.config.getCommonConfig().playerdamagereduction.with(FORMULA_DAMAGE_ARG, damageOrg).evaluate().getNumberValue().floatValue();
                if (ArmorDamage.config.getCommonConfig().debugprint)
                {
                    ArmorDamage.LOGGER.info("Normalizing player damage from: " + damage + " to:" + normalizedDamage);
                }

                damageContainers.peek().setNewDamage(normalizedDamage);
            }
            return normalizedDamage;
        }

        return damageOrg;
    }
}
