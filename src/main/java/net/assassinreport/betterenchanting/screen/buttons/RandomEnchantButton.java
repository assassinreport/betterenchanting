package net.assassinreport.betterenchanting.screen.buttons;

import net.assassinreport.betterenchanting.screen.NewEnchantingScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class RandomEnchantButton extends ClickableWidget {

    private boolean isActiveState = false;
    private String glyphText = "";
    private static final String GLYPHS = "ᚠᚢᚦᚨᚱᚲᚷᚹᚺᚾᛁᛃᛈᛇᛉᛊᛏᛒᛖᛗᛚᛜᛞᛟ";
    private final PressAction onPress;

    public interface PressAction {
        void onPress(RandomEnchantButton button);
    }

    public RandomEnchantButton(int x, int y, int width, int height, PressAction onPress) {
        super(x, y, width, height, ScreenTexts.EMPTY);
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

    @Override
    public void onClick(Click click, boolean doubled) {
        if (this.isActiveState) {
            this.onPress.onPress(this);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int row = 0;
        if (isActiveState) {
            row = isHovered() ? 2 : 1;
        }

        context.drawTexture(RenderPipelines.GUI_TEXTURED, NewEnchantingScreen.TEXTURE, getX(), getY(), 222, row * 15, 40, 15, 2816, 300);

        if (glyphText.isEmpty()) return;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        Formatting textColor = (isHovered() && isActiveState) ? Formatting.YELLOW : Formatting.DARK_GRAY;

        net.minecraft.text.Text runeText = net.minecraft.text.Text.literal(glyphText).setStyle(
                Style.EMPTY
                        .withFont(new StyleSpriteSource.Font(Identifier.of("minecraft", "default")))
                        .withColor(textColor)
        );

        int textWidth = textRenderer.getWidth(runeText.getString());
        int textHeight = textRenderer.fontHeight;

        int textX = getX() + (getWidth() - textWidth) / 2;
        int textY = getY() + (15 - textHeight) / 2 + 1;

        context.drawText(textRenderer, runeText, textX, textY, 0xFFFFFFFF, false);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}