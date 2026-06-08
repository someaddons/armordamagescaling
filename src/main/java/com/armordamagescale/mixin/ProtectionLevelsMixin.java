package com.armordamagescale.mixin;

import com.armordamagescale.ArmorDamage;
import com.cupboard.util.RegistryLookup;
import com.cupboard.util.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Enchantment.class)
public class ProtectionLevelsMixin
{
    /**
     * Observes if the protection applies to this damage
     */
    @Unique
    private float before = 0;

    @Inject(method = "modifyDamageProtection", at = @At("HEAD"))
    private void remember(
        final ServerLevel serverLevel,
        final int enchantmentLevel,
        final ItemStack item,
        final Entity victim,
        final DamageSource source,
        final MutableFloat protection,
        final CallbackInfo ci)
    {
        before = protection.floatValue();
    }

    @Inject(method = "modifyDamageProtection", at = @At("RETURN"))
    private void adjust(
        final ServerLevel serverLevel,
        final int enchantmentLevel,
        final ItemStack item,
        final Entity victim,
        final DamageSource source,
        final MutableFloat protection,
        final CallbackInfo ci)
    {
        float diff = protection.floatValue() - before;
        if (diff != 0)
        {
            Enchantment self = (Enchantment) (Object) this;
            final ResourceLocation resourceLocation = RegistryLookup.getID(serverLevel, Registries.ENCHANTMENT, self);
            if (resourceLocation != null)
            {
                final double multiplier = ArmorDamage.config.getCommonConfig().protectionLevels.getOrDefault(resourceLocation.toString(), -770);
                if (multiplier != -770)
                {
                    protection.add(-diff);
                    protection.add(multiplier * enchantmentLevel);
                }
            }
        }
    }
}
