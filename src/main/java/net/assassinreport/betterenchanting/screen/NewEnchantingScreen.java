package net.assassinreport.betterenchanting.screen;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.assassinreport.betterenchanting.network.ClientToServerPackets;
import net.assassinreport.betterenchanting.screen.animations.Book;
import net.assassinreport.betterenchanting.screen.buttons.RandomEnchantButton;
import net.assassinreport.betterenchanting.screen.buttons.SelectableEnchantButtons;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jspecify.annotations.NullMarked;

import java.util.*;

public class NewEnchantingScreen extends AbstractContainerScreen<NewEnchantingScreenHandler> {

    private Book bookRenderer;
    private RandomEnchantButton randomEnchantButton;
    private boolean showButton = false;

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath((BetterEnchanting.MOD_ID), "textures/gui/enchanting_menu_1_21.png");
    private static final Identifier EMPTY_HELMET = Identifier.fromNamespaceAndPath("minecraft", "container/slot/helmet");
    private static final Identifier EMPTY_CHEST = Identifier.fromNamespaceAndPath("minecraft", "container/slot/chestplate");
    private static final Identifier EMPTY_LEGS = Identifier.fromNamespaceAndPath("minecraft", "container/slot/leggings");
    private static final Identifier EMPTY_BOOTS = Identifier.fromNamespaceAndPath("minecraft", "container/slot/boots");
    private static final Identifier EMPTY_SHIELD = Identifier.fromNamespaceAndPath("minecraft", "container/slot/shield");

    private final List<SelectableEnchantButtons> enchantmentButtons = new ArrayList<>();
    private List<Holder<Enchantment>> orderedEnchantments = new ArrayList<>();
    private Map<Holder<Enchantment>, Integer> weights = new HashMap<>();

    @SuppressWarnings("unchecked")
    private static final ResourceKey<Enchantment>[] ORDERED_ENCHANTMENTS = new ResourceKey[] {
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

    public NewEnchantingScreen(NewEnchantingScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 220, 222);
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        if (this.minecraft.level != null) {
            this.bookRenderer = new Book(
                    new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK))
            );

            var registry = this.minecraft.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            orderedEnchantments = Arrays.stream(ORDERED_ENCHANTMENTS)
                    .<Holder<Enchantment>>map(key -> registry.get(key).orElseThrow())
                    .toList();

            Map<Holder<Enchantment>, Integer> dynamicWeights = new HashMap<>();
            int currentTextureOffset = 0;
            for (Holder<Enchantment> enchant : orderedEnchantments) {
                dynamicWeights.put(enchant, currentTextureOffset);
                currentTextureOffset += enchant.value().getMaxLevel();
            }
            this.weights = dynamicWeights;
        }

        createSelectableEnchantButtons();

        titleLabelY = 1000;
        inventoryLabelY = 1000;
    }

    private void drawEmptySlot(GuiGraphicsExtractor context, int slotId, Identifier texture, int offsetX, int offsetY) {
        if (!menu.slots.get(slotId).hasItem()) {
            context.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    texture,
                    this.leftPos + offsetX,
                    this.topPos + offsetY,
                    16, 16
            );
        }
    }

    private void drawScreenBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        context.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x, y,
                0f, 0f,
                220, 222,
                2750, 282
        );

        bookRenderer.render(context, x, y, delta);

        if (!this.menu.slots.get(menu.armorSlotIds[0]).hasItem()) {
            drawEmptySlot(context, menu.armorSlotIds[0], EMPTY_HELMET, 8, 140);
        }
        if (!this.menu.slots.get(menu.armorSlotIds[1]).hasItem()) {
            drawEmptySlot(context, menu.armorSlotIds[1], EMPTY_CHEST, 8, 158);
        }
        if (!this.menu.slots.get(menu.armorSlotIds[2]).hasItem()) {
            drawEmptySlot(context, menu.armorSlotIds[2], EMPTY_LEGS, 8, 176);
        }
        if (!this.menu.slots.get(menu.armorSlotIds[3]).hasItem()) {
            drawEmptySlot(context, menu.armorSlotIds[3], EMPTY_BOOTS, 8, 198);
        }
        if (!this.menu.slots.get(menu.offhandSlotId).hasItem()) {
            drawEmptySlot(context, menu.offhandSlotId, EMPTY_SHIELD, 196, 198);
        }
    }

    private void doTick() {
        ItemStack stackInSlot = menu.getSlot(0).getItem();
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
                this.removeWidget(randomEnchantButton);
                randomEnchantButton = null;
            }
        }
    }

    @NullMarked
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        this.drawScreenBackground(graphics, mouseX, mouseY, delta);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        this.extractTooltip(graphics, mouseX, mouseY);

        for (SelectableEnchantButtons btn : enchantmentButtons) {
            if (btn.isHovered()) {
                Holder<Enchantment> enchant = btn.getEnchantment();
                int unlockedLevel = 1;
                Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> cached = menu.blockEntity.getCachedEnchantments();
                for (var entry : cached.entrySet()) {
                    if (entry.getKey().enchantment() == enchant && entry.getValue() >= 6) {
                        unlockedLevel = Math.max(unlockedLevel, entry.getKey().level());
                    }
                }

                Component formattedName = Enchantment.getFullname(enchant, unlockedLevel);
                graphics.setTooltipForNextFrame(this.font, formattedName, mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        ItemStack stackInSlot = menu.getSlot(0).getItem();
        if (stackInSlot.isEmpty() && !enchantmentButtons.isEmpty()) {
            for (SelectableEnchantButtons btn : enchantmentButtons) {
                this.removeWidget(btn);
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
            this.removeWidget(randomEnchantButton);
        }

        int btnX = this.leftPos + 6;
        int btnY = this.topPos + 105;
        int btnWidth = 40;
        int btnHeight = 15;

        ItemStack inputStack = menu.getSlot(0).getItem();

        boolean canAffordAny = false;
        if (!inputStack.isEmpty() && minecraft.player != null) {
            canAffordAny = menu.blockEntity.canAffordAny(minecraft.player, inputStack);
        }

        randomEnchantButton = new RandomEnchantButton(
                btnX,
                btnY,
                btnWidth,
                btnHeight,
                btn -> ClientToServerPackets.sendRandomEnchantPacket()
        );

        randomEnchantButton.setActiveState(canAffordAny);

        addRenderableWidget(randomEnchantButton);
    }

    private void createSelectableEnchantButtons() {
        for (SelectableEnchantButtons btn : enchantmentButtons) {
            this.removeWidget(btn);
        }
        enchantmentButtons.clear();

        ItemStack inputStack = menu.getSlot(0).getItem();
        if (inputStack.isEmpty()) return;

        int iconWidth = 22;
        int iconHeight = 20;
        int iconStartX = this.leftPos + 50;
        int iconStartY = this.topPos + 8;

        int layoutIndex = 0;

        for (Holder<Enchantment> enchantment : orderedEnchantments) {
            if (!(enchantment.value().isSupportedItem(inputStack) || inputStack.is(Items.BOOK))) continue;

            int btnX = iconStartX + (layoutIndex % 7) * (iconWidth + 1);
            int btnY = iconStartY + (layoutIndex / 7) * (iconHeight + 1);
            layoutIndex++;

            SelectableEnchantButtons button = new SelectableEnchantButtons(
                    btnX, btnY,
                    iconWidth, iconHeight,
                    enchantment,
                    menu.blockEntity,
                    weights,
                    btn -> {
                        int unlockedLevel = 1;
                        Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> cached = menu.blockEntity.getCachedEnchantments();

                        for (var entry : cached.entrySet()) {
                            if (entry.getKey().enchantment() == enchantment && entry.getValue() >= 6) {
                                unlockedLevel = Math.max(unlockedLevel, entry.getKey().level());
                            }
                        }

                        ClientToServerPackets.sendSelectEnchantmentPacket(enchantment, unlockedLevel);
                    }
            );

            this.addRenderableWidget(button);
            enchantmentButtons.add(button);
        }
    }
}