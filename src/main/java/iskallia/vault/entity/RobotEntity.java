package iskallia.vault.entity;

import iskallia.vault.util.EntityHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class RobotEntity extends IronGolemEntity {

    public RobotEntity(EntityType<? extends IronGolemEntity> type, World worldIn) {
        super(type, worldIn);
        EntityHelper.changeSize(this, 2f);
    }

    @Override
    protected void dropLoot(DamageSource damageSource, boolean attackedRecently) { }

}
