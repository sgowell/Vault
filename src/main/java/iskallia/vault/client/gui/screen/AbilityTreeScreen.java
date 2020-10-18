package iskallia.vault.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import iskallia.vault.Vault;
import iskallia.vault.ability.AbilityTree;
import iskallia.vault.client.gui.component.AbilityDialog;
import iskallia.vault.client.gui.helper.Rectangle;
import iskallia.vault.client.gui.helper.UIHelper;
import iskallia.vault.client.gui.widget.AbilityWidget;
import iskallia.vault.container.AbilityTreeContainer;
import iskallia.vault.init.ModConfigs;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.LinkedList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class AbilityTreeScreen extends ContainerScreen<AbilityTreeContainer> {

    public static final ResourceLocation UI_RESOURCE = new ResourceLocation(Vault.MOD_ID, "textures/gui/ability-tree.png");
    public static final ResourceLocation BACKGROUNDS_RESOURCE = new ResourceLocation(Vault.MOD_ID, "textures/gui/ability-tree-bgs.png");

    private Vector2f viewportTranslation;
    private float viewportScale;
    private boolean dragging;
    private Vector2f grabbedPos;

    private List<AbilityWidget> abilityWidgets;
    private AbilityWidget selectedWidget;
    private AbilityDialog abilityDialog;

    public AbilityTreeScreen(AbilityTreeContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, new StringTextComponent("Ability Tree Screen!"));
        this.viewportTranslation = new Vector2f(0, 0);
        this.viewportScale = 1f;
        this.dragging = false;
        this.grabbedPos = new Vector2f(0, 0);

        this.selectedWidget = null;
        this.abilityWidgets = new LinkedList<>();
        refreshWidgets();
    }

    public void refreshWidgets() {
        this.abilityWidgets.clear();

        AbilityTree abilityTree = getContainer().getAbilityTree();
        ModConfigs.ABILITIES_GUI.getStyles().forEach((abilityName, style) -> {
            this.abilityWidgets.add(new AbilityWidget(
                    ModConfigs.ABILITIES.getByName(abilityName),
                    abilityTree,
                    style
            ));
        });

        this.abilityDialog = new AbilityDialog(abilityTree);
    }

    public Rectangle getContainerBounds() {
        Rectangle bounds = new Rectangle();
        bounds.x0 = 30; //px
        bounds.y0 = 60; //px
        bounds.x1 = (int) (width * 0.7); // Responsiveness ayyyyy
        bounds.y1 = height - 30;
        return bounds;
    }

    /* --------------------------------------------------- */

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Rectangle containerBounds = getContainerBounds();

        if (containerBounds.contains((int) mouseX, (int) mouseY)) {
            this.dragging = true;
            this.grabbedPos = new Vector2f((float) mouseX, (float) mouseY);

            Vector2f midpoint = getContainerBounds().midpoint();
            int containerMouseX = (int) ((mouseX - midpoint.x) / viewportScale - viewportTranslation.x);
            int containerMouseY = (int) ((mouseY - midpoint.y) / viewportScale - viewportTranslation.y);
            for (AbilityWidget abilityWidget : abilityWidgets) {
                if (abilityWidget.isMouseOver(containerMouseX, containerMouseY)
                        && abilityWidget.mouseClicked(containerMouseX, containerMouseY, button)) {
                    if (this.selectedWidget != null) this.selectedWidget.deselect();
                    this.selectedWidget = abilityWidget;
                    this.selectedWidget.select();
                    this.abilityDialog.setAbilityGroup(this.selectedWidget.getAbilityGroup());
                    break;
                }
            }

        } else {
            this.abilityDialog.mouseClicked((int) mouseX, (int) mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (dragging) {
            float dx = (float) (mouseX - grabbedPos.x) / viewportScale;
            float dy = (float) (mouseY - grabbedPos.y) / viewportScale;
            this.viewportTranslation = new Vector2f(
                    viewportTranslation.x + dx,
                    viewportTranslation.y + dy);
            this.grabbedPos = new Vector2f((float) mouseX, (float) mouseY);

        } else {
            this.abilityDialog.mouseMoved((int) mouseX, (int) mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Rectangle containerBounds = getContainerBounds();

        if (containerBounds.contains((int) mouseX, (int) mouseY)) {
            Vector2f midpoint = containerBounds.midpoint();
            double zoomingX = (mouseX - midpoint.x) / viewportScale + viewportTranslation.x;
            double zoomingY = (mouseY - midpoint.y) / viewportScale + viewportTranslation.y;

            int wheel = delta < 0 ? -1 : 1;

            double zoomTargetX = (zoomingX - viewportTranslation.x) / viewportScale;
            double zoomTargetY = (zoomingY - viewportTranslation.y) / viewportScale;

            viewportScale += 0.25 * wheel * viewportScale;
            viewportScale = (float) MathHelper.clamp(viewportScale, 0.5, 5);

            viewportTranslation = new Vector2f(
                    (float) (-zoomTargetX * viewportScale + zoomingX),
                    (float) (-zoomTargetY * viewportScale + zoomingY)
            );
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    /* --------------------------------------------------- */

    @Override
    protected void
    drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        renderBackground(matrixStack);
    }

    @Override
    protected void
    drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        // For some reason, without this it won't render :V
        this.font.func_243248_b(matrixStack,
                new StringTextComponent(""),
                (float) this.titleX, (float) this.titleY,
                4210752);
    }

    @Override
    public void
    render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        UIHelper.renderOverflowHidden(matrixStack,
                this::renderContainerBackground,
                ms -> this.renderSkillTree(ms, mouseX, mouseY, partialTicks));

        Rectangle containerBounds = getContainerBounds();

        AbilityTree abilityTree = getContainer().getAbilityTree();
        renderLabel(matrixStack, "Vault Level: " + abilityTree.getVaultLevel(), 15);
        if (abilityTree.getUnspentSkillPts() > 0) {
            renderLabel(matrixStack,
                    abilityTree.getUnspentSkillPts() + " unspent skill point(s)",
                    containerBounds.getHeight() - 40);
        }
        renderContainerBorders(matrixStack);
        renderContainerTabs(matrixStack);
        Rectangle dialogBounds = new Rectangle();
        dialogBounds.x0 = containerBounds.x1 + 15;
        dialogBounds.y0 = containerBounds.y0 - 18;
        dialogBounds.x1 = width - 21;
        dialogBounds.y1 = height - 21;

        abilityDialog
                .setBounds(dialogBounds)
                .render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void
    renderContainerTabs(MatrixStack matrixStack) {
        Rectangle containerBounds = getContainerBounds();

        int tabWidth = 28;
        int gap = 3; // px

        for (int i = 0; i < 3; i++) {
            blit(matrixStack,
                    containerBounds.x0 + 5 + i * (tabWidth + gap),
                    containerBounds.y0 - 25 - 17,
                    63, 0, tabWidth, 25);
        }
    }

    private void
    renderContainerBorders(MatrixStack matrixStack) {
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bindTexture(UI_RESOURCE);

        Rectangle containerBounds = getContainerBounds();

        RenderSystem.enableBlend();

        blit(matrixStack, containerBounds.x0 - 9, containerBounds.y0 - 18,
                0, 0, 15, 24);
        blit(matrixStack, containerBounds.x1 - 7, containerBounds.y0 - 18,
                18, 0, 15, 24);
        blit(matrixStack, containerBounds.x0 - 9, containerBounds.y1 - 7,
                0, 27, 15, 16);
        blit(matrixStack, containerBounds.x1 - 7, containerBounds.y1 - 7,
                18, 27, 15, 16);

        matrixStack.push();
        matrixStack.translate(containerBounds.x0 + 6, containerBounds.y0 - 18, 0);
        matrixStack.scale(containerBounds.x1 - containerBounds.x0 - 13, 1, 1);
        blit(matrixStack, 0, 0,
                16, 0, 1, 24);
        matrixStack.translate(0, containerBounds.y1 - containerBounds.y0 + 11, 0);
        blit(matrixStack, 0, 0,
                16, 27, 1, 16);
        matrixStack.pop();

        matrixStack.push();
        matrixStack.translate(containerBounds.x0 - 9, containerBounds.y0 + 6, 0);
        matrixStack.scale(1, containerBounds.y1 - containerBounds.y0 - 13, 1);
        blit(matrixStack, 0, 0,
                0, 25, 15, 1);
        matrixStack.translate(containerBounds.x1 - containerBounds.x0 + 2, 0, 0);
        blit(matrixStack, 0, 0,
                18, 25, 15, 1);
        matrixStack.pop();
    }

    private void
    renderLabel(MatrixStack matrixStack, String text, int yLevel) {
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bindTexture(UI_RESOURCE);

        FontRenderer fontRenderer = minecraft.fontRenderer;

        Rectangle containerBounds = getContainerBounds();
        int textWidth = fontRenderer.getStringWidth(text);

        matrixStack.push();
        matrixStack.translate(containerBounds.x1, containerBounds.y0, 0);

        matrixStack.translate(-9, yLevel, 0);
        blit(matrixStack, 0, 0, 143, 36, 9, 24);

        int gap = 5;
        int remainingWidth = textWidth + 2 * gap;
        matrixStack.translate(-remainingWidth, 0, 0);
        while (remainingWidth > 0) {
            blit(matrixStack, 0, 0, 136, 36, 6, 24);
            remainingWidth -= 6;
            matrixStack.translate(Math.min(6, remainingWidth), 0, 0);
        }

        matrixStack.translate(-textWidth - 2 * gap - 6, 0, 0);
        blit(matrixStack, 0, 0, 121, 36, 14, 24);

        fontRenderer.drawString(matrixStack, text,
                14 + gap, 9, 0xFF_443a1b);

        matrixStack.pop();
    }

    private void
    renderContainerBackground(MatrixStack matrixStack) {
        assert this.minecraft != null;

        this.minecraft.getTextureManager().bindTexture(BACKGROUNDS_RESOURCE);

        Rectangle containerBounds = getContainerBounds();

        // TODO: Include scale param
        int textureSize = 16;
        int currentX = containerBounds.x0;
        int currentY = containerBounds.y0;
        int uncoveredWidth = containerBounds.getWidth();
        int uncoveredHeight = containerBounds.getHeight();
        while (uncoveredWidth > 0) {
            while (uncoveredHeight > 0) {
                blit(matrixStack, currentX, currentY,
                        16 * 5, 0, // TODO: <-- depends on tab
                        Math.min(textureSize, uncoveredWidth),
                        Math.min(textureSize, uncoveredHeight)
                );
                uncoveredHeight -= textureSize;
                currentY += textureSize;
            }

            // Decrement
            uncoveredWidth -= textureSize;
            currentX += textureSize;

            // Reset
            uncoveredHeight = containerBounds.getHeight();
            currentY = containerBounds.y0;
        }
    }

    private void
    renderSkillTree(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableBlend();

        Vector2f midpoint = getContainerBounds().midpoint();

        matrixStack.push();
        matrixStack.translate(midpoint.x, midpoint.y, 0);
        matrixStack.scale(viewportScale, viewportScale, 1);
        matrixStack.translate(viewportTranslation.x, viewportTranslation.y, 0);

        int containerMouseX = (int) ((mouseX - midpoint.x) / viewportScale - viewportTranslation.x);
        int containerMouseY = (int) ((mouseY - midpoint.y) / viewportScale - viewportTranslation.y);

        for (AbilityWidget abilityWidget : abilityWidgets) {
            abilityWidget.render(matrixStack, containerMouseX, containerMouseY, partialTicks);
        }

        matrixStack.pop();
    }

}
