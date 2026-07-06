package net.assassinreport.betterenchanting.screen.buttons;

import net.assassinreport.betterenchanting.screen.NewEnchantingScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.NullMarked;

public class RandomEnchantButton extends AbstractWidget {

    private boolean isActiveState = false;
    private String glyphText = "";
    private static final String GLYPHS = "ᚠᚢᚦᚨᚱᚲᚷᚹᚺᚾᛁᛃᛈᛇᛉᛊᛏᛒᛖᛗᛚᛜᛞᛟ";
    private final PressAction onPress;

    public interface PressAction {
        void onPress(RandomEnchantButton button);
    }

    public RandomEnchantButton(int x, int y, int width, int height, PressAction onPress) {
        super(x, y, width, height, Component.empty());
        this.onPress = onPress;
    }

    public void randomizeGlyphs() {
        StringBuilder sb = new StringBuilder(6);
        char lastChar = '\0';
        for (int i = 0; i < 6; i++) {
            char nextChar;
            do {
                int index = (int) (Math.random() * GLYPHS.length());
                nextChar = GLYPHS.charAt(index);
            } while (nextChar == lastChar);
            sb.append(nextChar);
            lastChar = nextChar;
        }
        glyphText = sb.toString();
    }

    public void setActiveState(boolean value) {
        this.isActiveState = value;
    }

    @NullMarked
    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (this.isActiveState) {
            this.onPress.onPress(this);
        }
    }

    @NullMarked
    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int row = 0;
        if (isActiveState) {
            row = isHovered() ? 2 : 1;
        }

        context.blit(RenderPipelines.GUI_TEXTURED, NewEnchantingScreen.TEXTURE, getX(), getY(), 222, row * 15, 40, 15, 2816, 300);

        if (glyphText.isEmpty()) return;

        Font font = Minecraft.getInstance().font;

        Component runeText = Component.literal(glyphText).setStyle(
                Style.EMPTY
                        .withFont(FontDescription.DEFAULT)
                        .withColor(isHovered() ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY)
        );

        int textWidth = font.width(runeText);
        int textHeight = 9;

        int textX = getX() + (getWidth() - textWidth) / 2;
        int textY = getY() + (getHeight() - textHeight) / 2;

        context.text(font, runeText, textX, textY, 0xFFFFFFFF, false);
    }

    @NullMarked
    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}