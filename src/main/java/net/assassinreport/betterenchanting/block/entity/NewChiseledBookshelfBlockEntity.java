package net.assassinreport.betterenchanting.block.entity;

import com.mojang.logging.LogUtils;
import net.assassinreport.betterenchanting.block.custom.NewChiseledBookshelfBlock;
import net.assassinreport.betterenchanting.screen.NewChiseledBookshelfScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;

public class NewChiseledBookshelfBlockEntity extends BlockEntity implements ImplementedInventory, ExtendedMenuProvider<BlockPos> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SLOT_COUNT = 6;

    private final NonNullList<ItemStack> inventory =
            NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private int lastInteractedSlot = -1;
    private boolean wasSpecialFull = false;

    private static final BooleanProperty[] MY_SLOTS = {
            NewChiseledBookshelfBlock.SLOT_0_OCCUPIED,
            NewChiseledBookshelfBlock.SLOT_1_OCCUPIED,
            NewChiseledBookshelfBlock.SLOT_2_OCCUPIED,
            NewChiseledBookshelfBlock.SLOT_3_OCCUPIED,
            NewChiseledBookshelfBlock.SLOT_4_OCCUPIED,
            NewChiseledBookshelfBlock.SLOT_5_OCCUPIED
    };

    public NewChiseledBookshelfBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEW_CHISELED_BOOKSHELF_BLOCK_ENTITY, pos, state);
    }

    private boolean hasSixIdenticalEnchantedBooks() {
        ItemStack first = inventory.getFirst();
        if (first.isEmpty() || !first.is(Items.ENCHANTED_BOOK)) return false;

        for (int i = 1; i < SLOT_COUNT; i++) {
            ItemStack current = inventory.get(i);
            if (!ItemStack.isSameItemSameComponents(first, current)) {
                return false;
            }
        }
        return true;
    }

    // -----------------------State update-------------------------

    public void updateState(int interactedSlot) {
        if (interactedSlot < 0 || interactedSlot >= SLOT_COUNT) {
            LOGGER.error("Expected slot 0-5, got {}", interactedSlot);
            return;
        }

        lastInteractedSlot = interactedSlot;

        BlockState oldState = getBlockState();
        BlockState newState = oldState;

        for (int i = 0; i < SLOT_COUNT; i++) {
            newState = newState.setValue(MY_SLOTS[i], !inventory.get(i).isEmpty());
        }

        if (newState != oldState && level != null) {
            level.setBlock(worldPosition, newState, Block.UPDATE_ALL);
        }
    }

    // --------------------------Screen-------------------------------

    @NullMarked
    @Override
    public Component getDisplayName() {
        return Component.literal("New Chiseled Bookshelf");
    }

    @NullMarked
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new NewChiseledBookshelfScreenHandler(syncId, inv, this);
    }

    @NullMarked
    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    // ----------------------Inventory---------------------------

    @Override
    public void clearContent() {
        inventory.clear();
    }

    @NullMarked
    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @NullMarked
    @Override
    public ItemStack getItem(int slot) {
        return inventory.get(slot);
    }

    public int getLastInteractedSlot() {
        return lastInteractedSlot;
    }

    @NullMarked
    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack existing = inventory.get(slot);
        inventory.set(slot, ItemStack.EMPTY);

        if (!existing.isEmpty()) updateState(slot);
        return existing;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.is(ItemTags.BOOKSHELF_BOOKS)) return;

        ItemStack oldStack = inventory.get(slot);
        inventory.set(slot, stack);
        updateState(slot);
        setChanged();

        if (level == null || !(level instanceof ServerLevel)) return;

        if (!hasSixIdenticalEnchantedBooks()) {
            SoundEvent soundToPlay = stack.is(Items.ENCHANTED_BOOK)
                    ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED
                    : SoundEvents.CHISELED_BOOKSHELF_INSERT;
            level.playSound(null, worldPosition, soundToPlay, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    @NullMarked
    @Override
    public boolean canTakeItem(Container hopperContainer, int slot, ItemStack stack) {
        return hopperContainer.hasAnyMatching(target ->
                target.isEmpty() ||
                        (ItemStack.isSameItemSameComponents(stack, target)
                                && target.getCount() + stack.getCount()
                                <= Math.min(target.getMaxStackSize(), hopperContainer.getMaxStackSize()))
        );
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @NullMarked
    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(ItemTags.BOOKSHELF_BOOKS) && getItem(slot).isEmpty();
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (level == null || !(level instanceof ServerLevel)) return;

        boolean isSpecialFullNow = hasSixIdenticalEnchantedBooks();

        if (isSpecialFullNow && !wasSpecialFull) {
            level.playSound(null, worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0f, 1.0f);
        } else if (!isSpecialFullNow && wasSpecialFull) {
            level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        wasSpecialFull = isSpecialFullNow;

        for (BlockPos p : BlockPos.withinManhattan(worldPosition, 3, 3, 3)) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof NewEnchantingTableBlockEntity enchantingTable) {
                enchantingTable.sync();
            }
        }
    }

    // --------------------NBT----------------------
    @NullMarked
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
        output.putInt("last_interacted_slot", lastInteractedSlot);
        this.wasSpecialFull = hasSixIdenticalEnchantedBooks();
    }

    @NullMarked
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);
        lastInteractedSlot = input.getIntOr("last_interacted_slot", 0);
    }
}