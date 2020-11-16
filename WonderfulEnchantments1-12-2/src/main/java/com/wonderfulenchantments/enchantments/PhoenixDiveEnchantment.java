package com.wonderfulenchantments.enchantments;

import com.wonderfulenchantments.ConfigHandler;
import com.wonderfulenchantments.RegistryHandler;
import com.wonderfulenchantments.WonderfulEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentFrostWalker;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class PhoenixDiveEnchantment extends Enchantment {
	protected static List< Vec3d > positionsToGenerateParticles = new ArrayList<>();

	public PhoenixDiveEnchantment( String name ) {
		super( Rarity.RARE, EnumEnchantmentType.ARMOR_FEET, new EntityEquipmentSlot[]{ EntityEquipmentSlot.FEET } );

		this.setName( name );
		this.setRegistryName( WonderfulEnchantments.MOD_ID, name );
		RegistryHandler.ENCHANTMENTS.add( this );
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public int getMinEnchantability( int level ) {
		return 10 + 10 * ( level ) + ( ConfigHandler.Enchantments.PHOENIX_DIVE ? 0 : RegistryHandler.disableEnchantmentValue );
	}

	@Override
	public int getMaxEnchantability( int level ) {
		return this.getMinEnchantability( level ) + 30;
	}

	@Override
	public boolean canApplyTogether( Enchantment enchantment ) {
		return !( enchantment instanceof EnchantmentFrostWalker );
	}

	@SubscribeEvent
	public static void onFall( LivingFallEvent event ) {
		double distance = event.getDistance();

		if( distance > 3.0D ) {
			EntityLivingBase attacker = event.getEntityLiving();
			World world = attacker.getEntityWorld();

			int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel( RegistryHandler.PHOENIX_DIVE, attacker.getItemStackFromSlot( EntityEquipmentSlot.FEET ) );

			if( enchantmentLevel > 0 ) {
				double range = 5.0D;
				List< Entity > entities = world.getEntitiesWithinAABBExcludingEntity( attacker, attacker.getEntityBoundingBox().offset( -range, -attacker.height * 0.5D, -range ).expand( range * 2.0D, 0, range * 2.0D ) );
				for( Entity entity : entities )
					if( entity instanceof EntityLiving ) {
						EntityLivingBase target = ( EntityLiving )entity;
						target.setRevengeTarget( attacker );
						target.attackEntityFrom( DamageSource.ON_FIRE, ( float )Math.sqrt( enchantmentLevel * distance ) );
						target.setFire( 20 * ( 2 * enchantmentLevel ) );
					}

				positionsToGenerateParticles.add( attacker.getPositionVector() );
			}
		}
	}

	@SubscribeEvent
	public static void onUpdate( TickEvent.WorldTickEvent event ) {
		if( positionsToGenerateParticles.size() > 0 ) {
			WorldServer world = ( WorldServer )event.world;

			for( Vec3d position : positionsToGenerateParticles ) {
				for( double d = 0.0D; d < 3.0D; d++ )
					world.spawnParticle( EnumParticleTypes.FLAME, position.x, position.y, position.z, ( int )Math.pow( 5.0D - 2.0D, d + 1.0D ), 0.0625D, 0.125D, 0.0625D, ( 0.125D + 0.0625D ) * ( d + 1.0D ) * 0.025D );

				world.playSound( null, position.x, position.y, position.z, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.AMBIENT, 0.25F, 1.0F );
			}

			positionsToGenerateParticles.clear();
		}
	}

	@SubscribeEvent
	public static void onJump( LivingEvent.LivingJumpEvent event ) {
		EntityLivingBase entity = event.getEntityLiving();

		if( entity instanceof EntityPlayer ) {
			EntityPlayer player = ( EntityPlayer )entity;
			ItemStack boots = entity.getItemStackFromSlot( EntityEquipmentSlot.FEET );
			int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel( RegistryHandler.PHOENIX_DIVE, boots );

			if( player.isSneaking() && enchantmentLevel > 0 ) {
				double angleInRadians = Math.toRadians( player.rotationYaw + 90.0D );
				double factor = ( enchantmentLevel + 1 ) * 0.33334D;
				player.setVelocity( player.motionX + factor * Math.cos( angleInRadians ), player.motionY * ( 1.0D + ( enchantmentLevel + 1 ) * 0.25D ), player.motionZ + factor * Math.sin( angleInRadians ) );

				boots.damageItem( 3, player );
			}
		}
	}
}
