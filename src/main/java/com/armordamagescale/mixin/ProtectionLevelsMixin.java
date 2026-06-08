package com.armordamagescale.mixin;

import com.armordamagescale.ArmorDamage;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProtectionEnchantment.class)
public class ProtectionLevelsMixin
{
    @Shadow
    @Final
    public ProtectionEnchantment.Type type;

    @Inject(method = "getDamageProtection", at = @At("RETURN"), cancellable = true)
    private void adjust(final int enchantmentLevel, final DamageSource damageSource, final CallbackInfoReturnable<Integer> cir)
    {
        if (enchantmentLevel > 0)
        {
            if (type != null)
            {
                final double multiplier = ArmorDamage.config.getCommonConfig().protectionLevels.getOrDefault(mapType(type), -770);
                if (multiplier != -770)
                {
                    cir.setReturnValue((int) (multiplier * enchantmentLevel));
                }
            }
        }
    }

    @Unique
    private static String mapType(ProtectionEnchantment.Type type)
    {
        if (type == ProtectionEnchantment.Type.ALL)
        {
            return "minecraft:protection";
        }
        if (type == ProtectionEnchantment.Type.FIRE)
        {
            return "minecraft:fire_protection";
        }

        if (type == ProtectionEnchantment.Type.FALL)
        {
            return "minecraft:feather_falling";
        }

        if (type == ProtectionEnchantment.Type.EXPLOSION)
        {
            return "minecraft:blast_protection";
        }


        if (type == ProtectionEnchantment.Type.PROJECTILE)
        {
            return "minecraft:projectile_protection";
        }

        return type.name();
    }
}
