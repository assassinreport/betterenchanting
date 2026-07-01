package net.assassinreport.betterenchanting.screen;

import net.assassinreport.betterenchanting.block.entity.NewChiseledBookshelfBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NullMarked;

public class NewChiseledBookshelfScreenHandler extends AbstractContainerMenu {

    private final NewChiseledBookshelfBlockEntity inventory;
    public final NewChiseledBookshelfBlockEntity blockEntity;

    public NewChiseledBookshelfScreenHandler(int syncId, Inventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, playerInventory.player.level().getBlockEntity(pos));
    }

    public NewChiseledBookshelfScreenHandler(int syncId, Inventory playerInventory, BlockEntity blockEntity) {
        this(syncId, playerInventory, blockEntity, new SimpleContainerData(2));
    }

    public NewChiseledBookshelfScreenHandler(int syncId, Inventory playerInventory, BlockEntity blockEntity, ContainerData arrayPropertyDelegate) {
        super(ModScreenHandlers.NEW_CHISELED_BOOKSHELF_SCREEN_HANDLER, syncId);
        Player player = playerInventory.player;
        if (!(blockEntity instanceof NewChiseledBookshelfBlockEntity be)) {
            throw new IllegalStateException("Block entity is not a NewChiseledBookshelfBlockEntity!");
        }
        checkContainerSize(be, 6);
        this.inventory = be;
        this.blockEntity = be;
        inventory.startOpen(player);

        for (int i = 0; i < 6; i++) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 8) {
                @NullMarked
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ItemTags.BOOKSHELF_BOOKS);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addDataSlots(arrayPropertyDelegate);
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        int step = fromLast ? -1 : 1;
        int i = fromLast ? endIndex - 1 : startIndex;

        while (stack.getCount() > 0 && (fromLast ? i >= startIndex : i < endIndex)) {
            Slot slot = this.slots.get(i);
            ItemStack slotStack = slot.getItem();

            if (slotStack.isEmpty() && slot.mayPlace(stack)) {
                ItemStack oneItem = stack.copy();
                oneItem.setCount(1);
                slot.set(oneItem);
                slot.setChanged();

                stack.shrink(1);

                if (slot.container == this.inventory) {
                    this.blockEntity.updateState(slot.index);
                    this.blockEntity.setChanged();
                }
            }

            i += step;
        }

        return stack.isEmpty();
    }

    @NullMarked
    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot clickedSlot = this.slots.get(invSlot);

        if (clickedSlot.hasItem()) {
            ItemStack originalStack = clickedSlot.getItem();
            newStack = originalStack.copy();

            int bookshelfSize = this.inventory.getContainerSize();

            boolean changed;

            if (invSlot < bookshelfSize) {
                changed = this.moveItemStackTo(originalStack, bookshelfSize, this.slots.size(), true);
                if (!changed) return ItemStack.EMPTY;
                this.blockEntity.updateState(invSlot);
            } else {
                changed = this.moveItemStackTo(originalStack, 0, bookshelfSize, false);
                if (!changed) return ItemStack.EMPTY;
                for (int i = 0; i < bookshelfSize; i++) {
                    this.blockEntity.updateState(i);
                }
            }

            this.blockEntity.setChanged();

            if (originalStack.isEmpty()) {
                clickedSlot.set(ItemStack.EMPTY);
            } else {
                clickedSlot.setChanged();
            }
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
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 30 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 88));
        }
    }
}