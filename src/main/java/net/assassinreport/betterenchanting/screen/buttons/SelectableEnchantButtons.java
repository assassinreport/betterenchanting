package net.assassinreport.betterenchanting.screen.buttons;

import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.assassinreport.betterenchanting.screen.NewEnchantingScreen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Map;

public class SelectableEnchantButtons extends ClickableWidget {
    private final RegistryEntry<Enchantment> enchant;
    private final NewEnchantingTableBlockEntity blockEntity;
    private final Map<RegistryEntry<Enchantment>, Integer> weights;
    private final PressAction onPress;

    public interface PressAction {
        void onPress(SelectableEnchantButtons button);
    }

    public SelectableEnchantButtons(int x, int y, int width, int height,
                                    RegistryEntry<Enchantment> enchant,
                                    NewEnchantingTableBlockEntity blockEntity,
                                    Map<RegistryEntry<Enchantment>, Integer> weights,
                                    PressAction onPress) {
        super(x, y, width, height, ScreenTexts.EMPTY); // Fixes Text.empty() removal
        this.enchant = enchant;
        this.blockEntity = blockEntity;
        this.weights = weights;
        this.onPress = onPress;
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        this.onPress.onPress(this);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean active = false;
        int maxLevel = 1;

        Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> cached = blockEntity.getCachedEnchantments();
        for (var entry : cached.entrySet()) {
            if (entry.getKey().enchantment() == enchant && entry.getValue() >= 6) {
                active = true;
                maxLevel = Math.max(maxLevel, entry.getKey().level());
            }
        }

        int row = 0;
        if (active) {
            row = isHovered() ? 2 : 1;
        }

        int baseIndex = weights.getOrDefault(enchant, 0);
        int textureIndex = baseIndex + (maxLevel - 1);

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                NewEnchantingScreen.TEXTURE,
                getX(), getY(),
                (float) (textureIndex * 22),
                (float) (222 + row * 20),
                22, 20,
                2750, 282
        );
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    public RegistryEntry<Enchantment> getEnchantment() {
        return enchant;
    }
}