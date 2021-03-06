package iskallia.vault.item;

import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModSounds;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRelicBoosterPack extends Item {

    public ItemRelicBoosterPack(ItemGroup group, ResourceLocation id) {
        super(new Properties()
                .group(group)
                .maxStackSize(64));

        this.setRegistryName(id);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (!world.isRemote) {
            ItemStack heldStack = player.getHeldItem(hand);
            postEffects(world, player.getPositionVec());

            ItemVaultRelicPart randomPart = ModConfigs.VAULT_RELICS.getRandomPart();
            ItemStack itemStack = new ItemStack(randomPart);

            player.dropItem(itemStack, false, false);

            heldStack.shrink(1);
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, world, tooltip, flagIn);
    }

    public static void postEffects(World world, Vector3d position) {
        world.playSound(
                null,
                position.x,
                position.y,
                position.z,
                ModSounds.BOOSTER_PACK_SFX,
                SoundCategory.PLAYERS,
                1f, 1f
        );

        ((ServerWorld)world).spawnParticle(ParticleTypes.DRAGON_BREATH,
                position.x,
                position.y,
                position.z,
                500,
                1, 1, 1,
                0.5
        );
    }

}
