package com.wonderfulenchantments.mixin;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin( Item.class )
public abstract class MixinItem {
	@Shadow( aliases = { "this$0" } )
	@Inject( method = "getEnchantmentValue ()I", at = @At( "RETURN" ), cancellable = true )
	protected void getEnchantmentValue( CallbackInfoReturnable< Integer > callback ) {}
}
