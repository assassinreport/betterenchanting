package net.assassinreport.betterenchanting.screen;

import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NewEnchantingScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    public final NewEnchantingTableBlockEntity blockEntity;
    private final PlayerEntity player;
    private final World world;
    private final BlockPos pos;

    private static final int TABLE_SLOT = 1;

    private static final int MAIN_INV_START = 1;
    private static final int MAIN_INV_END = 28;

    private static final int HOTBAR_START = 28;
    private static final int HOTBAR_END = 37;

    private static final int ARMOR_START = 37;
    private static final int ARMOR_END = 41;

    private static final int OFFHAND_SLOT = 41;

    public final int[] armorSlotIds = new int[4];
    public int offhandSlotId;

    // -----------------------------
    //          Constructors
    // -----------------------------

    public NewEnchantingScreenHandler(int syncId, PlayerInventory playerInventory,
                                      BlockEntity blockEntity) {
        super(ModScreenHandlers.NEW_ENCHANTING_SCREEN_HANDLER, syncId);
        this.player = playerInventory.player;

        if (!(blockEntity instanceof NewEnchantingTableBlockEntity be)) {
            throw new IllegalStateException("Block entity is not a NewEnchantingTableBlockEntity!");
        }

        checkSize(be, 1);
        this.inventory = be;
        this.blockEntity = be;
        this.world = be.getWorld();
        this.pos = be.getPos();
        inventory.onOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 18, 96) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.BOOK)
                        || stack.isOf(Items.ENCHANTED_BOOK)
                        || stack.isEnchantable()
                        || !EnchantmentHelper.getEnchantments(stack).isEmpty();
            }

            @Override
            public int getMaxItemCount() {
                return 1;
            }

            @Override
            public void setStack(ItemStack stack) {
                super.setStack(stack);
                if (!stack.isEmpty()) {
                    playBookOpenSound();
                }
            }
        });
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addArmorSlots(playerInventory);
        addOffhandSlot(playerInventory);
    }

    // ---------------- ClientToServer -------------------

    public NewEnchantingScreenHandler(int syncId, PlayerInventory inventory, BlockPos pos) {
        this(syncId, inventory, inventory.player.getEntityWorld().getBlockEntity(pos));
    }

    // ------------------ Enchanting----------------------

    private boolean hasEnchantment(ItemStack stack, RegistryEntry<Enchantment> enchantment, int level) {
        if (stack.isEmpty()) return false;
        int existing = EnchantmentHelper.getEnchantments(stack).getLevel(enchantment);
        return existing >= level;
    }

    public void tryRandomEnchant() {
        if (this.world instanceof ServerWorld) {
            this.blockEntity.tryEnchantItem(player);
            this.sendContentUpdates();
        }
        playEnchantSound();
    }

    public void trySelectableEnchant(ServerPlayerEntity player, RegistryEntry<Enchantment> enchantment, int level) {
        ItemStack input = getSlot(0).getStack();
        if (input.isEmpty()) return;
        if (input.isOf(Items.BOOK) || input.isOf(Items.ENCHANTED_BOOK)) return;
        if (hasEnchantment(input, enchantment, level)) return;

        input.addEnchantment(enchantment, level);
        getSlot(0).markDirty();
        sendContentUpdates();
        playEnchantSound();
    }

    // ------------------------Inventory----------------------------

    private void insertOnlyOneItemIntoSlot(Slot slot, ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        slot.setStack(copy);
        stack.decrement(1);
        slot.markDirty();
    }

    @Override
    protected boolean insertItem(ItemStack stack, int start, int end, boolean reverse) {
        boolean inserted = false;
        int i = reverse ? end - 1 : start;

        while (i >= start && i < end) {
            Slot slot = slots.get(i);
            if (!slot.hasStack() && slot.canInsert(stack)) {
                insertOnlyOneItemIntoSlot(slot, stack);
                inserted = true;
                if (stack.isEmpty()) break;
            }
            i += reverse ? -1 : 1;
        }

        return inserted;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        Slot slot = this.slots.get(invSlot);
        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack originalStack = slot.getStack();
        ItemStack newStack = originalStack.copy();

        if (invSlot < TABLE_SLOT) {
            if (!this.insertItem(originalStack, HOTBAR_START, HOTBAR_END + 1, false)) {
                if (!this.insertItem(originalStack, OFFHAND_SLOT, OFFHAND_SLOT + 1, false)) {
                    if (!this.insertItem(originalStack, MAIN_INV_START, MAIN_INV_END + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        } else {
            if (!this.insertItem(originalStack, 0, TABLE_SLOT, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (originalStack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 30 + l * 18, 158 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 30 + i * 18, 216));
        }
    }

    private void addArmorSlots(PlayerInventory inv) {
        EquipmentSlot[] types = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int[][] pos = {{8,158},{8,176},{8,194},{8,216}};

        for (int i = 0; i < 4; i++) {
            EquipmentSlot type = types[i];
            int slotIndex = 39 - i;

            armorSlotIds[i] = addSlot(new Slot(inv, slotIndex, pos[i][0], pos[i][1]) {
                @Override
                public boolean canInsert(ItemStack s) {
                    EquippableComponent equippable = s.get(DataComponentTypes.EQUIPPABLE);
                    return equippable != null && equippable.slot() == type;
                }

                @Override
                public int getMaxItemCount() { return 1; }

                @Override
                public void setStack(ItemStack stack) {
                    super.setStack(stack);
                    if (!stack.isEmpty()) {
                        EquippableComponent equippable = stack.get(DataComponentTypes.EQUIPPABLE);
                        if (equippable != null) {
                            world.playSound(
                                    null,
                                    NewEnchantingScreenHandler.this.pos,
                                    equippable.equipSound().value(),
                                    SoundCategory.PLAYERS,
                                    1.0f,
                                    1.0f
                            );
                        }
                    }
                }
            }).id;
        }
    }

    private void addOffhandSlot(PlayerInventory playerInventory) {
        offhandSlotId = this.addSlot(new Slot(playerInventory, 40, 196, 216) {
            @Override
            public void setStack(ItemStack stack) {
                super.setStack(stack);
                if (!stack.isEmpty()) {
                    EquippableComponent equippable = stack.get(DataComponentTypes.EQUIPPABLE);
                    if (equippable != null) {
                        world.playSound(
                                null,
                                NewEnchantingScreenHandler.this.pos,
                                equippable.equipSound().value(),
                                SoundCategory.PLAYERS,
                                1.0f,
                                1.0f
                        );
                    }
                }
            }
        }).id;
    }

    // --------------------------
    //          Sounds
    // --------------------------

    private void playEnchantSound() {
        world.playSound(
                null,
                pos,
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.BLOCKS,
                1.0f,
                1.0f
        );
    }

    private void playBookOpenSound() {
        world.playSound(
                null,
                pos,
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.BLOCKS,
                1.0f,
                0.5f
        );
    }
}