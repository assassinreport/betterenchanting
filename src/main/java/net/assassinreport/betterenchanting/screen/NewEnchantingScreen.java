package net.assassinreport.betterenchanting.screen;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.assassinreport.betterenchanting.network.ClientToServerPackets;
import net.assassinreport.betterenchanting.screen.animations.Book;
import net.assassinreport.betterenchanting.screen.buttons.RandomEnchantButton;
import net.assassinreport.betterenchanting.screen.buttons.SelectableEnchantButtons;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public class NewEnchantingScreen extends HandledScreen<NewEnchantingScreenHandler> {

    private Book bookRenderer;
    private RandomEnchantButton randomEnchantButton;
    private boolean showButton = false;

    public static final Identifier TEXTURE = Identifier.of(BetterEnchanting.MOD_ID, "textures/gui/enchanting_menu_1_21.png");
    private static final Identifier EMPTY_HELMET = Identifier.ofVanilla("container/slot/helmet");
    private static final Identifier EMPTY_CHEST  = Identifier.ofVanilla("container/slot/chestplate");
    private static final Identifier EMPTY_LEGS   = Identifier.ofVanilla("container/slot/leggings");
    private static final Identifier EMPTY_BOOTS  = Identifier.ofVanilla("container/slot/boots");
    private static final Identifier EMPTY_SHIELD = Identifier.ofVanilla("container/slot/shield");

    private final List<SelectableEnchantButtons> enchantmentButtons = new ArrayList<>();
    private List<RegistryEntry<Enchantment>> orderedEnchantments = new ArrayList<>();
    private Map<RegistryEntry<Enchantment>, Integer> weights = new HashMap<>();

    private static final RegistryKey<Enchantment>[] ORDERED_ENCHANTMENTS = new RegistryKey[] {
            Enchantments.AQUA_AFFINITY,
            Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.BLAST_PROTECTION,
            Enchantments.BREACH,
            Enchantments.CHANNELING,
            Enchantments.DENSITY,
            Enchantments.BINDING_CURSE,
            Enchantments.VANISHING_CURSE,
            Enchantments.DEPTH_STRIDER,
            Enchantments.EFFICIENCY,
            Enchantments.FEATHER_FALLING,
            Enchantments.FIRE_ASPECT,
            Enchantments.FIRE_PROTECTION,
            Enchantments.FLAME,
            Enchantments.FORTUNE,
            Enchantments.FROST_WALKER,
            Enchantments.IMPALING,
            Enchantments.INFINITY,
            Enchantments.KNOCKBACK,
            Enchantments.LOOTING,
            Enchantments.LOYALTY,
            Enchantments.LUCK_OF_THE_SEA,
            Enchantments.LURE,
            Enchantments.MENDING,
            Enchantments.MULTISHOT,
            Enchantments.PIERCING,
            Enchantments.POWER,
            Enchantments.PROJECTILE_PROTECTION,
            Enchantments.PROTECTION,
            Enchantments.PUNCH,
            Enchantments.QUICK_CHARGE,
            Enchantments.RESPIRATION,
            Enchantments.RIPTIDE,
            Enchantments.SILK_TOUCH,
            Enchantments.SMITE,
            Enchantments.SOUL_SPEED,
            Enchantments.SWEEPING_EDGE,
            Enchantments.SWIFT_SNEAK,
            Enchantments.THORNS,
            Enchantments.SHARPNESS,
            Enchantments.UNBREAKING,
            Enchantments.WIND_BURST
    };

    public NewEnchantingScreen(NewEnchantingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        this.backgroundWidth = 220;
        this.backgroundHeight = 222;
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        if (this.client != null && this.client.world != null) {
            this.bookRenderer = new Book(
                    new BookModel(this.client.getLoadedEntityModels().getModelPart(EntityModelLayers.BOOK)),
                    Identifier.of("textures/entity/enchanting_table_book.png")
            );

            var registry = this.client.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            orderedEnchantments = Arrays.stream(ORDERED_ENCHANTMENTS)
                    .map(key -> (RegistryEntry<Enchantment>) registry.getOptional(key).orElseThrow())
                    .toList();

            Map<RegistryEntry<Enchantment>, Integer> dynamicWeights = new HashMap<>();
            int currentTextureOffset = 0;
            for (RegistryEntry<Enchantment> enchant : orderedEnchantments) {
                dynamicWeights.put(enchant, currentTextureOffset);
                currentTextureOffset += enchant.value().getMaxLevel();
            }
            this.weights = dynamicWeights;
        }

        createSelectableEnchantButtons();

        titleY = 1000;
        playerInventoryTitleY = 1000;
    }

    private void drawEmptySlot(DrawContext context, int slotId, Identifier texture, int offsetX, int offsetY) {
        if (!handler.slots.get(slotId).hasStack()) {
            context.drawGuiTexture(
                    RenderPipelines.GUI_TEXTURED,
                    texture,
                    this.x + offsetX,
                    this.y + offsetY,
                    16, 16
            );
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x, y,
                0f, 0f,
                220, 222,
                2750, 282
        );

        bookRenderer.render(context, x, y, delta);

        if (!this.handler.slots.get(handler.armorSlotIds[0]).hasStack()) {
            drawEmptySlot(context, handler.armorSlotIds[0], EMPTY_HELMET, 8, 140);
        }

        if (!this.handler.slots.get(handler.armorSlotIds[1]).hasStack()) {
            drawEmptySlot(context, handler.armorSlotIds[1], EMPTY_CHEST, 8, 158);
        }

        if (!this.handler.slots.get(handler.armorSlotIds[2]).hasStack()) {
            drawEmptySlot(context, handler.armorSlotIds[2], EMPTY_LEGS, 8, 176);
        }

        if (!this.handler.slots.get(handler.armorSlotIds[3]).hasStack()) {
            drawEmptySlot(context, handler.armorSlotIds[3], EMPTY_BOOTS, 8, 198);
        }

        if (!this.handler.slots.get(handler.offhandSlotId).hasStack()) {
            drawEmptySlot(context, handler.offhandSlotId, EMPTY_SHIELD, 196, 198);
        }
    }

    private void doTick() {
        ItemStack stackInSlot = handler.getSlot(0).getStack();
        bookRenderer.tick(stackInSlot, !stackInSlot.isEmpty());
    }

    private void updateBookAndButton(boolean hasItem) {
        if (hasItem) {
            if (!showButton && randomEnchantButton == null) {
                showButton = true;
                randomEnchantButton();
                randomEnchantButton.randomizeGlyphs();
            }
        } else {
            showButton = false;
            if (randomEnchantButton != null) {
                this.remove(randomEnchantButton);
                randomEnchantButton = null;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        for (SelectableEnchantButtons btn : enchantmentButtons) {
            if (btn.isHovered()) {
                RegistryEntry<Enchantment> enchant = btn.getEnchantment();
                int unlockedLevel = 1;
                Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> cached = handler.blockEntity.getCachedEnchantments();
                for (var entry : cached.entrySet()) {
                    if (entry.getKey().enchantment() == enchant && entry.getValue() >= 6) {
                        unlockedLevel = Math.max(unlockedLevel, entry.getKey().level());
                    }
                }

                Text formattedName = Enchantment.getName(enchant, unlockedLevel);
                context.drawTooltip(this.textRenderer, formattedName, mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();

        ItemStack stackInSlot = handler.getSlot(0).getStack();
        if (stackInSlot.isEmpty() && !enchantmentButtons.isEmpty()) {
            for (SelectableEnchantButtons btn : enchantmentButtons) {
                this.remove(btn);
            }
            enchantmentButtons.clear();
        } else if (!stackInSlot.isEmpty() && enchantmentButtons.isEmpty()) {
            createSelectableEnchantButtons();
        }

        doTick();

        updateBookAndButton(!stackInSlot.isEmpty());
    }

    private void randomEnchantButton() {
        if (randomEnchantButton != null) {
            this.remove(randomEnchantButton);
        }

        int btnX = this.x + 6;
        int btnY = this.y + 105;
        int btnWidth = 40;
        int btnHeight = 15;

        ItemStack inputStack = handler.getSlot(0).getStack();

        boolean canAffordAny = false;
        if (!inputStack.isEmpty() && client != null && client.player != null) {
            canAffordAny = handler.blockEntity.canAffordAny(client.player, inputStack);
        }

        randomEnchantButton = new RandomEnchantButton(
                btnX,
                btnY,
                btnWidth,
                btnHeight,
                btn -> ClientToServerPackets.sendRandomEnchantPacket()
        );

        randomEnchantButton.setActiveState(canAffordAny);

        addDrawableChild(randomEnchantButton);
    }

    private void createSelectableEnchantButtons() {
        for (SelectableEnchantButtons btn : enchantmentButtons) {
            this.remove(btn);
        }
        enchantmentButtons.clear();

        ItemStack inputStack = handler.getSlot(0).getStack();
        if (inputStack.isEmpty()) return;

        int iconWidth = 22;
        int iconHeight = 20;
        int iconStartX = this.x + 50;
        int iconStartY = this.y + 8;

        int layoutIndex = 0;

        for (RegistryEntry<Enchantment> enchantment : orderedEnchantments) {
            if (!(enchantment.value().isAcceptableItem(inputStack) || inputStack.isOf(Items.BOOK))) continue;

            int btnX = iconStartX + (layoutIndex % 7) * (iconWidth + 1);
            int btnY = iconStartY + (layoutIndex / 7) * (iconHeight + 1);
            layoutIndex++;

            SelectableEnchantButtons button = new SelectableEnchantButtons(
                    btnX, btnY,
                    iconWidth, iconHeight,
                    enchantment,
                    handler.blockEntity,
                    weights,
                    btn -> {
                        int unlockedLevel = 1;
                        Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> cached = handler.blockEntity.getCachedEnchantments();

                        for (var entry : cached.entrySet()) {
                            if (entry.getKey().enchantment() == enchantment && entry.getValue() >= 6) {
                                unlockedLevel = Math.max(unlockedLevel, entry.getKey().level());
                            }
                        }

                        ClientToServerPackets.sendSelectEnchantmentPacket(enchantment, unlockedLevel);
                    }
            );

            this.addDrawableChild(button);
            enchantmentButtons.add(button);
        }
    }
}