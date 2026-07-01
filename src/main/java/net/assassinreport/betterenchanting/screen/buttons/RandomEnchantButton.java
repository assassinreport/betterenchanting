package net.assassinreport.betterenchanting.screen.buttons;

import net.assassinreport.betterenchanting.screen.NewEnchantingScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.NullMarked;

public class RandomEnchantButton extends Button {

    private boolean isActiveState = false;
    private String glyphText = "";
    private static final String GLYPHS = "ᚠᚢᚦᚨᚱᚲᚷᚹᚺᚾᛁᛃᛈᛇᛉᛊᛏᛒᛖᛗᛚᛜᛞᛟ";

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

    public RandomEnchantButton(int x, int y, int width, int height, Button.OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, btn -> Component.empty());
    }

    public void setActiveState(boolean value) {
        this.isActiveState = value;
        this.active = value;
    }

    @NullMarked
    @Override
    protected void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int row = 0;
        if (isActiveState) {
            row = isHovered() ? 2 : 1;
        }

        context.blit(RenderPipelines.GUI_TEXTURED, NewEnchantingScreen.TEXTURE, getX(), getY(), 220, row * 15, 40, 15, 2750, 282);

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
}