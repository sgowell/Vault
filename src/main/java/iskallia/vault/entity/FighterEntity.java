package iskallia.vault.entity;

import iskallia.vault.util.SkinProfile;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.lang.reflect.Field;

public class FighterEntity extends ZombieEntity {

	public SkinProfile skin;
	public String lastName = "Fighter";
	public float sizeMultiplier = 1.0F;

	public FighterEntity(EntityType<? extends ZombieEntity> type, World world) {
		super(type, world);

		this.setCustomName(new StringTextComponent(this.lastName));

		if(!this.world.isRemote) {
			this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.rand.nextFloat() * 0.25d + 0.25d);
		} else {
			this.skin = new SkinProfile();
		}
	}

	public ResourceLocation getLocationSkin() {
		return this.skin.getLocationSkin();
	}

	@Override
	public boolean isChild() {
		return false;
	}

	@Override
	protected boolean shouldBurnInDay() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if(this.dead)return;

		if(this.world.isRemote) {
			String name = this.getCustomName().getString();

			if (!this.lastName.equals(name)) {
				this.skin.updateSkin(name);
				this.lastName = name;
			}
		} else {
			double amplitude = this.getMotion().squareDistanceTo(0.0D, this.getMotion().getY(), 0.0D);

			if(amplitude > 0.004D) {
				this.setSprinting(true);
				this.getJumpController().setJumping();
			} else {
				this.setSprinting(false);
			}
		}
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putFloat("SizeMultiplier", this.sizeMultiplier);
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);

		if(compound.contains("SizeMultiplier", Constants.NBT.TAG_FLOAT)) {
			this.changeSize(compound.getFloat("SizeMultiplier"));
		}
	}

	@Override
	public float getRenderScale() {
		return super.getRenderScale();
	}

	@Override
	public EntitySize getSize(Pose pose) {
		Field sizeField = Entity.class.getDeclaredFields()[79]; //Entity.size
		sizeField.setAccessible(true);

		try {
			return (EntitySize)sizeField.get(this);
		} catch(IllegalAccessException e) {
			e.printStackTrace();
			return super.getSize(pose);
		}
	}

	public void changeSize(float m) {
		if(m == this.sizeMultiplier)return;
		Field sizeField = Entity.class.getDeclaredFields()[79]; //Entity.size
		sizeField.setAccessible(true);

		try {
			sizeField.set(this, this.getSize(Pose.STANDING).scale(this.sizeMultiplier = m));
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}

		this.recalculateSize();
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntitySize size) {
		return super.getStandingEyeHeight(pose, size) * this.sizeMultiplier;
	}

	@Override
	public ILivingEntityData onInitialSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason reason, ILivingEntityData spawnData, CompoundNBT dataTag) {
		this.setCustomName(this.getCustomName());
		this.setBreakDoorsAItask(true);
		this.setCanPickUpLoot(true);
		this.enablePersistence();

		//Good ol' easter egg.
		if(this.rand.nextInt(100) == 0) {
			ChickenEntity chicken = EntityType.CHICKEN.create(this.world);
			chicken.setLocationAndAngles(this.getPosX(), this.getPosY(), this.getPosZ(), this.rotationYaw, 0.0F);
			chicken.onInitialSpawn(world, difficulty, reason, spawnData, dataTag);
			chicken.setChickenJockey(true);
			((ServerWorld)this.world).summonEntity(chicken);
			this.startRiding(chicken);
		}

		return spawnData;
	}

	@Override
	protected void dropLoot(DamageSource damageSource, boolean attackedRecently) {
		super.dropLoot(damageSource, attackedRecently);
		if(this.world.isRemote())return;

		/* Drop the head.
		if(!this.lastName.equals(this.getCustomName().getString())) {
			ItemStack headDrop = new ItemStack(Items.PLAYER_HEAD, 1);
			CompoundNBT nbt = new CompoundNBT();
			nbt.putString("SkullOwner", this.getCustomName().getString());
			headDrop.setTag(nbt);
			this.entityDropItem(headDrop, 0.0F);
		}*/
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		if (!this.world.isRemote) {
			((ServerWorld)this.world).spawnParticle(ParticleTypes.SWEEP_ATTACK, this.getPosX(), this.getPosY(),
					this.getPosZ(), 1, 0.0f, 0.0f, 0.0f, 0);

			this.world.playSound(null, this.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
					SoundCategory.PLAYERS, 1.0f, this.rand.nextFloat() - this.rand.nextFloat());
		}

		return super.attackEntityAsMob(entity);
	}

}