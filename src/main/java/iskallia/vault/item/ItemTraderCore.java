package iskallia.vault.item;

import iskallia.vault.Vault;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.util.nbt.NBTSerializer;
import iskallia.vault.vending.Product;
import iskallia.vault.vending.Trade;
import iskallia.vault.vending.TraderCore;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemTraderCore extends Item {


    public ItemTraderCore(ItemGroup group, ResourceLocation id) {
        super(new Properties()
                .group(group)
                .maxStackSize(1));

        this.setRegistryName(id);
    }

    public static ItemStack generate(String nickname, int value, boolean megahead) {
        List<Trade> trades = ModConfigs.VENDING_CONFIG.TRADES.stream().filter(trade -> trade.isValid())
                .collect(Collectors.toList());

        Collections.shuffle(trades);

        Optional<Trade> trade = trades.stream().findFirst();
        if (trade.isPresent())
            return getStack(new TraderCore(nickname, trade.get(), value, megahead));

        Vault.LOGGER.error("Attempted to generate a Trader Circuit.. No Trades in config.");
        return ItemStack.EMPTY;
    }

    public static ItemStack getStack(TraderCore core) {
        ItemStack stack = new ItemStack(ModItems.TRADER_CORE, 1);
        CompoundNBT nbt = new CompoundNBT();
        try {
            nbt.put("core", NBTSerializer.serialize(core));
            stack.setTag(nbt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stack;
    }

    public static TraderCore toTraderCore(ItemStack itemStack) {
        CompoundNBT nbt = itemStack.getTag();
        if (nbt == null) return null;
        try {
            return NBTSerializer.deserialize(TraderCore.class, nbt.getCompound("core"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        CompoundNBT nbt = stack.getOrCreateTag();
        if (nbt.contains("core")) {
            TraderCore core = null;
            try {
                core = NBTSerializer.deserialize(TraderCore.class, (CompoundNBT) nbt.get("core"));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            Trade trade = core.getTrade();
            if (!trade.isValid()) return;

            Product buy = trade.getBuy();
            Product extra = trade.getExtra();
            Product sell = trade.getSell();
            tooltip.add(new StringTextComponent("Name: " + core.getName()));
            tooltip.add(new StringTextComponent("Trades: "));
            if (buy != null && buy.isValid()) {
                StringTextComponent comp = new StringTextComponent(" - Buy: ");
                comp.append(new TranslationTextComponent(buy.getItem().getTranslationKey()))
                        .append(new StringTextComponent(" x" + buy.getAmount()));
                tooltip.add(comp);
            }
            if (extra != null && extra.isValid()) {
                StringTextComponent comp = new StringTextComponent(" - Extra: ");
                comp.append(new TranslationTextComponent(extra.getItem().getTranslationKey()))
                        .append(new StringTextComponent(" x" + extra.getAmount()));
                tooltip.add(comp);
            }
            if (sell != null && sell.isValid()) {
                StringTextComponent comp = new StringTextComponent(" - Sell: ");
                comp.append(new TranslationTextComponent(sell.getItem().getTranslationKey()))
                        .append(new StringTextComponent(" x" + sell.getAmount()));
                tooltip.add(comp);
            }
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return new StringTextComponent("Trader Circuit");
    }

}
