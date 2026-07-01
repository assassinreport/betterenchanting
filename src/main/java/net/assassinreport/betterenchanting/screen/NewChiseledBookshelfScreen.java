package net.assassinreport.betterenchanting.screen;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NullMarked;

public class NewChiseledBookshelfScreen extends AbstractContainerScreen<NewChiseledBookshelfScreenHandler> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(BetterEnchanting.MOD_ID, "textures/gui/chiseled_bookshelf_inventory.png");


    public NewChiseledBookshelfScreen(NewChiseledBookshelfScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 112);
    }

    @Override
    protected void init(){
        super.init();

        titleLabelY = 1000;
        inventoryLabelY = 1000;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 176, 112, 176, 112);
        super.extractContents(graphics, mouseX, mouseY, delta);
    }

    @NullMarked
    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        extractContents(context, mouseX, mouseY, delta);
        super.extractRenderState(context, mouseX, mouseY, delta);
        extractTooltip(context, mouseX, mouseY);
    }
}