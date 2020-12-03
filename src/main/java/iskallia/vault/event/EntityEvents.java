package iskallia.vault.event;

import iskallia.vault.Vault;
import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.block.VaultDoorBlock;
import iskallia.vault.entity.EntityScaler;
import iskallia.vault.entity.FighterEntity;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModEntities;
import iskallia.vault.init.ModSounds;
import iskallia.vault.world.data.VaultRaidData;
import iskallia.vault.world.gen.PortalPlacer;
import iskallia.vault.world.raid.VaultRaid;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityEvents {

	@SubscribeEvent
	public static void onEntityTick(LivingEvent.LivingUpdateEvent event) {
		if(event.getEntity().world.isRemote
				|| !(event.getEntity() instanceof MonsterEntity)
				|| event.getEntity().world.getDimensionKey() != Vault.VAULT_KEY
				|| event.getEntity().getTags().contains("VaultScaled"))return;

		MonsterEntity entity = (MonsterEntity)event.getEntity();
		VaultRaid raid = VaultRaidData.get((ServerWorld) entity.world).getAt(entity.getPosition());
		if(raid == null)return;

		EntityScaler.scale(entity, raid.level, new Random());
		entity.getTags().add("VaultScaled");
		entity.enablePersistence();
	}

	@SubscribeEvent
	public static void onEntityTick2(LivingEvent.LivingUpdateEvent event) {
		if(event.getEntity().world.isRemote
				|| !(event.getEntity() instanceof FighterEntity)
				|| event.getEntity().world.getDimensionKey() != Vault.ARENA_KEY)return;

		((FighterEntity)event.getEntity()).enablePersistence();
	}

	@SubscribeEvent
	public static void onEntityTick3(EntityEvent.EntityConstructing event) {
		if(event.getEntity().world.isRemote
				|| !(event.getEntity() instanceof AreaEffectCloudEntity)
				|| event.getEntity().world.getDimensionKey() != Vault.VAULT_KEY)return;

		event.getEntity().getServer().enqueue(new TickDelayedTask(event.getEntity().getServer().getTickCounter() + 2, () -> {
			if(!event.getEntity().getTags().contains("vault_door"))return;

			BlockPos pos = event.getEntity().getPosition();
			BlockState state = event.getEntity().world.getBlockState(pos);

			if(state.getBlock() == Blocks.IRON_DOOR) {
				BlockState newState = VaultDoorBlock.VAULT_DOORS.get(event.getEntity().world.rand.nextInt(VaultDoorBlock.VAULT_DOORS.size())).getDefaultState()
						.with(DoorBlock.FACING, state.get(DoorBlock.FACING))
						.with(DoorBlock.OPEN, state.get(DoorBlock.OPEN))
						.with(DoorBlock.HINGE, state.get(DoorBlock.HINGE))
						.with(DoorBlock.POWERED, state.get(DoorBlock.POWERED))
						.with(DoorBlock.HALF, state.get(DoorBlock.HALF));

				PortalPlacer placer = new PortalPlacer((pos1, random, facing) -> null, (pos1, random, facing) -> Blocks.BEDROCK.getDefaultState());
				placer.place(event.getEntity().world, pos, state.get(DoorBlock.FACING).rotateYCCW(), 1, 2);
				placer.place(event.getEntity().world, pos.offset(state.get(DoorBlock.FACING).getOpposite()), state.get(DoorBlock.FACING).rotateYCCW(), 1, 2);

				event.getEntity().world.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), 27);
				event.getEntity().world.setBlockState(pos, newState, 11);
				event.getEntity().world.setBlockState(pos.up(), newState.with(DoorBlock.HALF, DoubleBlockHalf.UPPER), 11);
			}

			for(int x = -30; x <= 30; x++) {
				for(int z = -30; z <= 30; z++) {
					for(int y = -10; y <= 10; y++) {
						BlockPos c = pos.add(x, y, z);
						BlockState s = event.getEntity().world.getBlockState(c);

						if(s.getBlock() == Blocks.PINK_WOOL) {
							event.getEntity().world.setBlockState(c, Blocks.CHEST.getDefaultState()
									.with(ChestBlock.FACING, Direction.byHorizontalIndex(event.getEntity().world.rand.nextInt(4))), 2);
							TileEntity te = event.getEntity().world.getTileEntity(c);

							if(te instanceof ChestTileEntity) {
								((ChestTileEntity)te).setLootTable(Vault.id("chest/treasure"), 0L);
							}
						}
					}
				}
			}

			event.getEntity().remove();
		}));
	}

	@SubscribeEvent
	public static void onEntityTick4(LivingEvent.LivingUpdateEvent event) {
		if(event.getEntity().world.isRemote
				|| event.getEntity().world.getDimensionKey() != Vault.VAULT_KEY
				|| !event.getEntity().getTags().contains("vault_boss"))return;

		Entity entity = event.getEntity();

		FighterEntity boss = ModEntities.FIGHTER.create(event.getEntity().world).changeSize(2.0F);
		boss.setLocationAndAngles(entity.getPosX(), entity.getPosY(), entity.getPosZ(), entity.rotationYaw, entity.rotationPitch);
		((ServerWorld)entity.world).summonEntity(boss);

		boss.getTags().add("VaultBoss");
		boss.bossInfo.setVisible(true);
		boss.setCustomName(new StringTextComponent("Boss"));
		boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0F);
		boss.setHealth(100.0F);

		VaultRaid raid = VaultRaidData.get((ServerWorld)entity.world).getAt(entity.getPosition());

		if(raid != null) {
			EntityScaler.scale(boss, raid.level + 5, new Random());
		}

		entity.remove();
	}

	@SubscribeEvent
	public static void onEntityTick5(LivingEvent.LivingUpdateEvent event) {
		if(event.getEntity().world.isRemote
				|| event.getEntity().world.getDimensionKey() != Vault.VAULT_KEY
				|| !event.getEntity().getTags().contains("vault_obelisk"))return;

		event.getEntityLiving().world.setBlockState(event.getEntityLiving().getPosition(), ModBlocks.OBELISK.getDefaultState());
		event.getEntityLiving().remove();
	}

	@SubscribeEvent
	public static void onEntityDeath(LivingDropsEvent event) {
		if(event.getEntity().world.isRemote
				|| event.getEntity().world.getDimensionKey() != Vault.VAULT_KEY
				|| !event.getEntity().getTags().contains("VaultBoss"))return;

		ServerWorld world = (ServerWorld)event.getEntityLiving().world;
		VaultRaid raid = VaultRaidData.get(world).getAt(event.getEntity().getPosition());

		if(raid != null) {
			raid.runIfPresent(world.getServer(), player -> {
				LootContext.Builder builder = (new LootContext.Builder(world)).withRandom(world.rand)
						.withParameter(LootParameters.THIS_ENTITY, event.getEntity())
						.withParameter(LootParameters.field_237457_g_, event.getEntity().getPositionVec())
						.withParameter(LootParameters.DAMAGE_SOURCE, event.getSource())
						.withNullableParameter(LootParameters.KILLER_ENTITY, event.getSource().getTrueSource())
						.withNullableParameter(LootParameters.DIRECT_KILLER_ENTITY, event.getSource().getImmediateSource())
						.withParameter(LootParameters.LAST_DAMAGE_PLAYER, player).withLuck(player.getLuck());

				LootContext ctx = builder.build(LootParameterSets.ENTITY);

				NonNullList<ItemStack> stacks = NonNullList.create();
				stacks.addAll(world.getServer().getLootTableManager().getLootTableFromLocation(Vault.id("chest/boss")).generate(ctx));
				ItemStack crate = VaultCrateBlock.getCrateWithLoot(ModBlocks.VAULT_CRATE, stacks);

				event.getEntity().entityDropItem(crate);

				FireworkRocketEntity fireworks = new FireworkRocketEntity(world, event.getEntity().getPosX(),
						event.getEntity().getPosY(), event.getEntity().getPosZ(), new ItemStack(Items.FIREWORK_ROCKET));
				world.addEntity(fireworks);
				//world.getServer().getLootTableManager().getLootTableFromLocation(Vault.id("chest/boss")).generate(ctx).forEach(stack -> {
				//	if(!player.addItemStackToInventory(stack)) {
				//		//TODO: drop the item at spawn
				//	}
				//});

				raid.won = true;
				raid.ticksLeft = 20 * 20;
				world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1.0F, 1.0F);
			});
		}
	}

	@SubscribeEvent
	public static void onEntityDrops(LivingDropsEvent event) {
		if(event.getEntity().world.isRemote
				|| event.getEntity().world.getDimensionKey() != Vault.VAULT_KEY)return;
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onEntitySpawn(LivingSpawnEvent.CheckSpawn event) {
		if(event.getEntity().getEntityWorld().getDimensionKey() == Vault.VAULT_KEY && !event.isSpawner()) {
			event.setCanceled(true);
		} else if(event.getEntity().getEntityWorld().getDimensionKey() == Vault.ARENA_KEY) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPlayerDeathInVaults(LivingDeathEvent event) {
		LivingEntity entityLiving = event.getEntityLiving();

		if (entityLiving.world.isRemote) return;

		if (entityLiving.world.getDimensionKey() != Vault.VAULT_KEY) return;

		if (entityLiving instanceof ServerPlayerEntity) {
			ServerPlayerEntity playerEntity = (ServerPlayerEntity) entityLiving;
            Vector3d position = playerEntity.getPositionVec();
            playerEntity.getServerWorld().playSound(null, position.x, position.y, position.z,
                    ModSounds.TIMER_KILL_SFX, SoundCategory.MASTER, 0.75F, 1F);
		}
	}

}
