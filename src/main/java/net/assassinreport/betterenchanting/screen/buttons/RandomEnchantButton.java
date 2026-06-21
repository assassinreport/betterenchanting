package net.assassinreport.betterenchanting.screen.buttons;

import net.assassinreport.betterenchanting.screen.NewEnchantingScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class RandomEnchantButton extends ButtonWidget {

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

    public RandomEnchantButton(int x, int y, int width, int height, PressAction onPress) {
        super(x, y, width, height, net.minecraft.text.Text.empty(), onPress, btn -> net.minecraft.text.Text.empty());
    }

    public void setActiveState(boolean value) {
        this.isActiveState = value;
        this.active = value;
    }

    @Override
    protected void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
        int row = 0;
        if (isActiveState) {
            row = isHovered() ? 2 : 1;
        }

        context.drawTexture(RenderPipelines.GUI_TEXTURED, NewEnchantingScreen.TEXTURE, getX(), getY(), 220, row * 15, 40, 15, 2750, 282);

        if (glyphText.isEmpty()) return;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        net.minecraft.text.Text runeText = net.minecraft.text.Text.literal(glyphText).setStyle(
                Style.EMPTY
                        .withFont(new StyleSpriteSource.Font(Identifier.of("minecraft", "default")))
                        .withColor(isHovered() ? Formatting.YELLOW : Formatting.DARK_GRAY)
        );

        int textWidth = textRenderer.getWidth(runeText.getString());
        int textHeight = textRenderer.fontHeight;

        int textX = getX() + (getWidth() - textWidth) / 2;
        int textY = getY() + (getHeight() - textHeight) / 2;

        context.drawText(textRenderer, runeText, textX, textY, 0xFFFFFFFF, false);
    }
}