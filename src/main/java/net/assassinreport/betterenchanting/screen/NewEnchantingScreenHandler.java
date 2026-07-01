package net.assassinreport.betterenchanting.screen;

import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NullMarked;

public class NewEnchantingScreenHandler extends AbstractContainerMenu {

    private final Container inventory;
    public final NewEnchantingTableBlockEntity blockEntity;
    private final Player player;
    private final Level world;
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

    public NewEnchantingScreenHandler(int syncId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModScreenHandlers.NEW_ENCHANTING_SCREEN_HANDLER, syncId);
        this.player = playerInventory.player;

        if (!(blockEntity instanceof NewEnchantingTableBlockEntity be)) {
            throw new IllegalStateException("Block entity is not a NewEnchantingTableBlockEntity!");
        }

        checkContainerSize(be, 1);
        this.inventory = be;
        this.blockEntity = be;
        this.world = be.getLevel();
        this.pos = be.getBlockPos();
        inventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 18, 78) {
            @NullMarked
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.BOOK)
                        || stack.is(Items.ENCHANTED_BOOK)
                        || stack.isEnchantable()
                        || !EnchantmentHelper.getEnchantmentsForCrafting(stack).isEmpty();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @NullMarked
            @Override
            public void set(ItemStack stack) {
                super.set(stack);
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

    public NewEnchantingScreenHandler(int syncId, Inventory inventory, BlockPos pos) {
        this(syncId, inventory, inventory.player.level().getBlockEntity(pos));
    }

    // ------------------ Enchanting----------------------

    private boolean hasEnchantment(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        if (stack.isEmpty()) return false;
        int existing = EnchantmentHelper.getEnchantmentsForCrafting(stack).getLevel(enchantment);
        return existing >= level;
    }

    public void tryRandomEnchant() {
        if (this.world instanceof ServerLevel) {
            this.blockEntity.tryEnchantItem(player);
            this.broadcastChanges();
        }
        playEnchantSound();
    }

    public void trySelectableEnchant(ServerPlayer player, Holder<Enchantment> enchantment, int level) {
        ItemStack input = getSlot(0).getItem();
        if (input.isEmpty()) return;
        if (input.is(Items.BOOK) || input.is(Items.ENCHANTED_BOOK)) return;
        if (hasEnchantment(input, enchantment, level)) return;

        input.enchant(enchantment, level);
        getSlot(0).setChanged();
        broadcastChanges();
        playEnchantSound();
    }

    // ------------------------Inventory----------------------------

    private void insertOnlyOneItemIntoSlot(Slot slot, ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        slot.set(copy);
        stack.shrink(1);
        slot.setChanged();
    }

    @NullMarked
    @Override
    protected boolean moveItemStackTo(ItemStack stack, int start, int end, boolean reverse) {
        boolean inserted = false;
        int i = reverse ? end - 1 : start;

        while (i >= start && i < end) {
            Slot slot = slots.get(i);
            if (!slot.hasItem() && slot.mayPlace(stack)) {
                insertOnlyOneItemIntoSlot(slot, stack);
                inserted = true;
                if (stack.isEmpty()) break;
            }
            i += reverse ? -1 : 1;
        }

        return inserted;
    }

    @NullMarked
    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        Slot slot = this.slots.get(invSlot);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack originalStack = slot.getItem();
        ItemStack newStack = originalStack.copy();

        if (invSlot < TABLE_SLOT) {
            if (!this.moveItemStackTo(originalStack, HOTBAR_START, HOTBAR_END + 1, false)) {
                if (!this.moveItemStackTo(originalStack, OFFHAND_SLOT, OFFHAND_SLOT + 1, false)) {
                    if (!this.moveItemStackTo(originalStack, MAIN_INV_START, MAIN_INV_END + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        } else {
            if (!this.moveItemStackTo(originalStack, 0, TABLE_SLOT, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (originalStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return newStack;
    }

    @NullMarked
    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 30 + l * 18, 140 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 30 + i * 18, 198));
        }
    }

    private void addArmorSlots(Inventory inv) {
        EquipmentSlot[] types = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int[][] pos = {{8,140},{8,158},{8,176},{8,198}};

        for (int i = 0; i < 4; i++) {
            EquipmentSlot type = types[i];
            int slotIndex = 39 - i;

            armorSlotIds[i] = addSlot(new Slot(inv, slotIndex, pos[i][0], pos[i][1]) {
                @NullMarked
                @Override
                public boolean mayPlace(ItemStack s) {
                    Equippable equippable = s.get(DataComponents.EQUIPPABLE);
                    return equippable != null && equippable.slot() == type;
                }
                @Override public int getMaxStackSize() { return 1; }}).index;
        }
    }

    private void addOffhandSlot(Inventory playerInventory) {
        offhandSlotId = this.addSlot(new Slot(playerInventory, 40, 196, 198)).index;
    }

    // --------------------------
    //          Sounds
    // --------------------------

    private void playEnchantSound() {
        world.playSound(
                null,
                pos,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
        );
    }

    private void playBookOpenSound() {
        world.playSound(
                null,
                pos,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.BLOCKS,
                1.0f,
                0.5f
        );
    }
}