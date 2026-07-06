package net.assassinreport.betterenchanting.screen.buttons;

import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.assassinreport.betterenchanting.screen.NewEnchantingScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

public class SelectableEnchantButtons extends AbstractWidget {
    private final Holder<Enchantment> enchant;
    private final NewEnchantingTableBlockEntity blockEntity;
    private final Map<Holder<Enchantment>, Integer> weights;
    private final PressAction onPress;

    public interface PressAction {
        void onPress(SelectableEnchantButtons button);
    }

    public SelectableEnchantButtons(int x, int y, int width, int height,
                                    Holder<Enchantment> enchant,
                                    NewEnchantingTableBlockEntity blockEntity,
                                    Map<Holder<Enchantment>, Integer> weights,
                                    PressAction onPress) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.enchant = enchant;
        this.blockEntity = blockEntity;
        this.weights = weights;
        this.onPress = onPress;
    }

    @NullMarked
    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.onPress.onPress(this);
    }

    @NullMarked
    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
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

        context.blit(
                RenderPipelines.GUI_TEXTURED,
                NewEnchantingScreen.TEXTURE,
                getX(), getY(),
                (float) (textureIndex * 22),
                (float) (240 + row * 20),
                22, 20,
                2816, 300
        );
    }

    @NullMarked
    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        this.defaultButtonNarrationText(builder);
    }

    public Holder<Enchantment> getEnchantment() {
        return enchant;
    }
}