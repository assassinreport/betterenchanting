    package net.assassinreport.betterenchanting.screen;

    import com.mojang.blaze3d.systems.RenderSystem;
    import net.assassinreport.betterenchanting.BetterEnchanting;
    import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
    import net.assassinreport.betterenchanting.network.ClientToServerPackets;
    import net.assassinreport.betterenchanting.screen.animations.Book;
    import net.assassinreport.betterenchanting.screen.buttons.RandomEnchantButton;
    import net.assassinreport.betterenchanting.screen.buttons.SelectableEnchantButtons;
    import net.minecraft.client.gui.DrawContext;
    import net.minecraft.client.gui.screen.ingame.HandledScreen;
    import net.minecraft.client.render.GameRenderer;
    import net.minecraft.client.render.entity.model.BookModel;
    import net.minecraft.client.render.entity.model.EntityModelLayers;
    import net.minecraft.enchantment.Enchantment;
    import net.minecraft.enchantment.Enchantments;
    import net.minecraft.entity.player.PlayerInventory;
    import net.minecraft.item.ItemStack;
    import net.minecraft.item.Items;
    import net.minecraft.text.Text;
    import net.minecraft.util.Identifier;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    public class NewEnchantingScreen extends HandledScreen<NewEnchantingScreenHandler> {

        private Book bookRenderer;

        private RandomEnchantButton randomEnchantButton;

        private boolean showButton = false;

        public static final Identifier TEXTURE = new Identifier(BetterEnchanting.MOD_ID, "textures/gui/enchanting_menu.png");
        private static final Identifier EMPTY_HELMET = new Identifier("minecraft", "textures/item/empty_armor_slot_helmet.png");
        private static final Identifier EMPTY_CHEST  = new Identifier("minecraft", "textures/item/empty_armor_slot_chestplate.png");
        private static final Identifier EMPTY_LEGS   = new Identifier("minecraft", "textures/item/empty_armor_slot_leggings.png");
        private static final Identifier EMPTY_BOOTS  = new Identifier("minecraft", "textures/item/empty_armor_slot_boots.png");
        private static final Identifier EMPTY_SHIELD = new Identifier("minecraft", "textures/item/empty_armor_slot_shield.png");

        // ---------------- Selectable Enchant Mapping -----------------------------------------
        // Listing out the enchants and mapping their order to the "enchanting_menu" GUI texture
        // --------------------------------------------------------------------------------------
        private final List<SelectableEnchantButtons> enchantmentButtons = new ArrayList<>();

        private static final Enchantment[] ORDERED_ENCHANTMENTS = new Enchantment[] {
                Enchantments.AQUA_AFFINITY,
                Enchantments.BANE_OF_ARTHROPODS,
                Enchantments.BLAST_PROTECTION,
                Enchantments.CHANNELING,
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
                Enchantments.SWEEPING,
                Enchantments.SWIFT_SNEAK,
                Enchantments.THORNS,
                Enchantments.SHARPNESS,
                Enchantments.UNBREAKING,
        };

        private static final Map<Enchantment, Integer> weights;
        static {
            Map<Enchantment, Integer> dynamicWeights = new HashMap<>();
            int currentTextureOffset = 0;
            for (Enchantment enchant : ORDERED_ENCHANTMENTS) {
                dynamicWeights.put(enchant, currentTextureOffset);
                currentTextureOffset += enchant.getMaxLevel();
            }
            weights = Map.copyOf(dynamicWeights);
        }

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

            this.bookRenderer = new Book(
                    new BookModel(this.client.getEntityModelLoader().getModelPart(EntityModelLayers.BOOK)),
                    new Identifier("textures/entity/enchanting_table_book.png")
            );

            createSelectableEnchantButtons();

            titleY = 1000;
            playerInventoryTitleY = 1000;
        }

        private void drawEmptySlot(DrawContext context, int slotId, Identifier texture, int offsetX, int offsetY) {
            if (!handler.slots.get(slotId).hasStack()) {
                context.drawTexture(texture, this.x + offsetX, this.y + offsetY, 0, 0, 16, 16, 16, 16);
            }
        }

        @Override
        protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.setShaderTexture(0, TEXTURE);

            int x = (width - backgroundWidth) / 2;
            int y = (height - backgroundHeight) / 2;

            context.drawTexture(TEXTURE, x, y, 0, 0, 220, 222, 2486, 282);

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
            this.renderBackground(context);
            super.render(context, mouseX, mouseY, delta);
            this.drawMouseoverTooltip(context, mouseX, mouseY);

            for (SelectableEnchantButtons btn : enchantmentButtons) {
                if (btn.isHovered()) {
                    Enchantment enchant = btn.getEnchantment();
                    int unlockedLevel = 1;
                    Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> cached = handler.blockEntity.getCachedEnchantments();
                    for (var entry : cached.entrySet()) {
                        if (entry.getKey().enchantment() == enchant && entry.getValue() >= 6) {
                            unlockedLevel = Math.max(unlockedLevel, entry.getKey().level());
                        }
                    }
                    context.drawTooltip(this.textRenderer, enchant.getName(unlockedLevel), mouseX, mouseY);
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
            if (!inputStack.isEmpty()) {
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

            for (Enchantment enchantment : ORDERED_ENCHANTMENTS) {
                if (!(enchantment.isAcceptableItem(inputStack) || inputStack.isOf(Items.BOOK))) continue;

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
                            int unlockedLevel = 0;
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