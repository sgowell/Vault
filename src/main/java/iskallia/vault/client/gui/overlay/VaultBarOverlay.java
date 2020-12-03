package iskallia.vault.client.gui.overlay;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import iskallia.vault.Vault;
import iskallia.vault.client.gui.helper.FontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class VaultBarOverlay {

    public static final ResourceLocation RESOURCE = new ResourceLocation(Vault.MOD_ID, "textures/gui/vault-hud.png");

    public static int vaultLevel;
    public static int vaultExp, tnl;
    public static int unspentSkillPoints;

    @SubscribeEvent
    public static void
    onPostRender(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.POTION_ICONS)
            return; // Render only on POTION_ICONS

        MatrixStack matrixStack = event.getMatrixStack();
        Minecraft minecraft = Minecraft.getInstance();
        int midX = minecraft.getMainWindow().getScaledWidth() / 2;
        int bottom = minecraft.getMainWindow().getScaledHeight();
        int right = minecraft.getMainWindow().getScaledWidth();

        String text = String.valueOf(vaultLevel);
        int textX = midX + 50 - (minecraft.fontRenderer.getStringWidth(text) / 2);
        int textY = bottom - 54;
        int barWidth = 85;
        float expPercentage = (float) vaultExp / tnl;


        if (VaultBarOverlay.unspentSkillPoints > 0) {
            ClientPlayerEntity player = minecraft.player;
            boolean iconsShowing = player != null && player.getActivePotionEffects().stream()
                    .anyMatch(EffectInstance::isShowIcon);
            int toastWidth = 160;
            minecraft.getTextureManager().bindTexture(RESOURCE);
            new AbstractGui() {}.blit(matrixStack, right - toastWidth - 1, iconsShowing ? 26 : 1,
                    3, 171, toastWidth, 32);
            minecraft.fontRenderer.drawString(matrixStack, unspentSkillPoints + " unspent skill point(s)",
                    right - toastWidth + 29, iconsShowing ? 39 : 14, 0xFF_a18959);
        }

        minecraft.getProfiler().startSection("vaultBar");
        minecraft.getTextureManager().bindTexture(RESOURCE);
        RenderSystem.enableBlend();
        minecraft.ingameGUI.blit(matrixStack,
                midX + 9, bottom - 48,
                1, 1, barWidth, 5);
        minecraft.ingameGUI.blit(matrixStack,
                midX + 9, bottom - 48,
                1, 7, (int) (barWidth * expPercentage), 5);
        FontHelper.drawStringWithBorder(matrixStack,
                text,
                textX, textY,
                0xFF_ffe637, 0x3c3400);
        minecraft.getProfiler().endSection();
    }

}
