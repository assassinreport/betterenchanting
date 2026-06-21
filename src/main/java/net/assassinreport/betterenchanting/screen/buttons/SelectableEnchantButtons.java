package net.assassinreport.betterenchanting.screen.buttons;

import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.assassinreport.betterenchanting.screen.NewEnchantingScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.Text;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Map;

public class SelectableEnchantButtons extends ButtonWidget {
    private final RegistryEntry<Enchantment> enchant;
    private final NewEnchantingTableBlockEntity blockEntity;
    private final Map<RegistryEntry<Enchantment>, Integer> weights;

    public SelectableEnchantButtons(int x, int y, int width, int height,
                                    RegistryEntry<Enchantment> enchant,
                                    NewEnchantingTableBlockEntity blockEntity,
                                    Map<RegistryEntry<Enchantment>, Integer> weights,
                                    PressAction onPress) {
        super(x, y, width, height, Text.empty(), onPress, button -> Text.empty());
        this.enchant = enchant;
        this.blockEntity = blockEntity;
        this.weights = weights;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
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
                RenderLayer::getGuiTextured,
                NewEnchantingScreen.TEXTURE,
                getX(), getY(),
                (float) (textureIndex * 22),
                (float) (222 + row * 20),
                22, 20,
                2750, 282
        );
    }

    public RegistryEntry<Enchantment> getEnchantment() {
        return enchant;
    }
}