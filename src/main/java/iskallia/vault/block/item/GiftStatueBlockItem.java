package iskallia.vault.block.item;

import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class GiftStatueBlockItem extends BlockItem {

    public GiftStatueBlockItem() {
        super(ModBlocks.GIFT_STATUE, new Properties()
                .group(ModItems.VAULT_MOD_GROUP)
                .maxStackSize(1));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        CompoundNBT nbt = stack.getTag();

        if (nbt != null) {
            CompoundNBT blockEntityTag = nbt.getCompound("BlockEntityTag");
            String nickname = blockEntityTag.getString("PlayerNickname");

            StringTextComponent text = new StringTextComponent(" Nickname: " + nickname);
            text.setStyle(Style.EMPTY.setColor(Color.fromInt(0xFF_ff9966)));
            tooltip.add(text);
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    public static ItemStack getStatueBlockItem(String nickname, int variant) {
        ItemStack itemStack = new ItemStack(ModBlocks.GIFT_STATUE_BLOCK_ITEM);

        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("PlayerNickname", nickname);
        nbt.putInt("Variant", variant);

        CompoundNBT stackNBT = new CompoundNBT();
        stackNBT.put("BlockEntityTag", nbt);
        itemStack.setTag(stackNBT);

        return itemStack;
    }

}
