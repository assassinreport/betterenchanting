package net.assassinreport.betterenchanting.block.entity;

import com.mojang.logging.LogUtils;
import net.assassinreport.betterenchanting.block.custom.NewChiseledBookshelfBlock;
import net.assassinreport.betterenchanting.screen.NewChiseledBookshelfScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;

public class NewChiseledBookshelfBlockEntity extends BlockEntity implements ImplementedInventory, ExtendedScreenHandlerFactory<BlockPos> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SLOT_COUNT = 6;

    private final DefaultedList<ItemStack> inventory =
            DefaultedList.ofSize(SLOT_COUNT, ItemStack.EMPTY);

    private int lastInteractedSlot = -1;

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

    // -----------------------State update-------------------------

    public void updateState(int interactedSlot) {
        if (interactedSlot < 0 || interactedSlot >= SLOT_COUNT) {
            LOGGER.error("Expected slot 0-5, got {}", interactedSlot);
            return;
        }

        lastInteractedSlot = interactedSlot;

        BlockState oldState = getCachedState();
        BlockState newState = oldState;

        for (int i = 0; i < SLOT_COUNT; i++) {
            newState = newState.with(MY_SLOTS[i], !inventory.get(i).isEmpty());
        }

        if (newState != oldState && world != null) {
            world.setBlockState(pos, newState, Block.NOTIFY_ALL);
        }
    }

    // --------------------------Screen-------------------------------

    @Override
    public Text getDisplayName() {
        return Text.literal("New Chiseled Bookshelf");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new NewChiseledBookshelfScreenHandler(syncId, inv, this);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return this.pos;
    }

    // ----------------------Inventory---------------------------

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public int size() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack existing = inventory.get(slot);
        inventory.set(slot, ItemStack.EMPTY);

        if (!existing.isEmpty()) updateState(slot);
        return existing;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return removeStack(slot, getStack(slot).getCount());
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (!stack.isIn(ItemTags.BOOKSHELF_BOOKS)) return;

        ItemStack oldStack = inventory.get(slot);
        inventory.set(slot, stack);
        updateState(slot);
        markDirty();

        if (world == null || !(world instanceof ServerWorld) || !oldStack.isEmpty() || stack.isEmpty()) {
            return;
        }

        SoundEvent soundToPlay = SoundEvents.ITEM_BOOK_PUT;

        world.playSound(null, pos, soundToPlay, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return hopperInventory.containsAny(target ->
                target.isEmpty() ||
                        (ItemStack.areItemsAndComponentsEqual(stack, target)
                                && target.getCount() + stack.getCount()
                                <= Math.min(target.getMaxCount(), hopperInventory.getMaxCountPerStack()))
        );
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return stack.isIn(ItemTags.BOOKSHELF_BOOKS) && getStack(slot).isEmpty();
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (world == null || world instanceof ServerWorld) return;

        // Notify nearby enchanting tables
        BlockPos.streamOutwards(pos, 3, 3, 3)
                .map(world::getBlockEntity)
                .filter(be -> be instanceof NewEnchantingTableBlockEntity)
                .forEach(be -> ((NewEnchantingTableBlockEntity) be).sync());
    }

    // --------------------NBT----------------------
    
    @Override
    public void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, inventory);
        view.putInt("last_interacted_slot", lastInteractedSlot);
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, inventory);
        lastInteractedSlot = view.getInt("last_interacted_slot", 0);
    }
}